package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.config.AiWorkflowConfig;
import com.coachai.dto.*;
import com.coachai.service.AiWorkflowService;
import com.coachai.service.IssueAnalysisRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 症状分析记录Controller
 */
@RestController
@RequestMapping("/issue-analysis-records")
@RequiredArgsConstructor
@Slf4j
public class IssueAnalysisRecordController {

    private final AiWorkflowService aiWorkflowService;
    private final AiWorkflowConfig aiWorkflowConfig;
    private final ObjectMapper objectMapper;
    private final IssueAnalysisRecordService issueAnalysisRecordService;


//    {
//        "username": "weile",
//        "bodyParts": ["膝盖", "小腿"],
//        "sport": "跑步",
//        "posture": ["前倾跑姿", "着地姿势"],
//        "description": "跑步时膝盖内侧疼痛，特别是在长距离跑步后疼痛加剧，感觉膝盖有压迫感"
//    }

    /**
     * 症状分析接口
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<IssueAnalysisRecordDTO.ApiResponse>> processIssueAnalysis(
            @RequestBody @Valid IssueAnalysisRequest request) {
        
        log.info("接收到姿态分析请求: username={}, sport={}, posture={}, imageLink={}", 
                request.getBodyParts(), request.getSport(), request.getPosture(), request.getDescription());
        
        try {
            // 2. 构建AI工作流请求
            AiWorkflowIssueRequest.ParamJson paramJson = AiWorkflowIssueRequest.ParamJson.builder()
                    .bodyParts(request.getBodyParts())
                    .sport(request.getSport())
                    .posture(request.getPosture())
                    .description(request.getDescription())
                    .build();
            
            AiWorkflowIssueRequest aiRequest = AiWorkflowIssueRequest.builder()
                    .apiCode(aiWorkflowConfig.getIssueAnalysis().getApiCode())
                    .stream(false)
                    .paramJson(paramJson)
                    .build();
            
            // 3. 调用AI工作流
            log.info("开始调用AI工作流进行姿态分析");
            AiWorkflowIssueResponse aiResponse = aiWorkflowService.callIssueAnalysisWorkflow(aiRequest);
            
            // 4. 解析AI工作流响应
            AiWorkflowIssueResponse.StructData structData = aiWorkflowService.parseIssueWorkflowResponse(aiResponse);
            
            // 5. 解析DiagnosisData
            AiWorkflowIssueResponse.DiagnosisData diagnosisData = aiWorkflowService.parseDiagnosisData(structData);
            
            // 6. 重新组合数据，将StructData中的poseReference和rehabilitationVideos解析为结构体并设置到DiagnosisData中
            if (diagnosisData != null) {
                // 解析poseReference JSON字符串为结构体
                List<AiWorkflowIssueResponse.PoseReference> parsedPoseReferences = new ArrayList<>();
                if (structData.getPoseReference() != null) {
                    for (String poseRefJson : structData.getPoseReference()) {
                        try {
                            AiWorkflowIssueResponse.PoseReference poseRef = objectMapper.readValue(poseRefJson, AiWorkflowIssueResponse.PoseReference.class);
                            parsedPoseReferences.add(poseRef);
                        } catch (Exception e) {
                            log.warn("解析poseReference失败: {}, JSON: {}", e.getMessage(), poseRefJson);
                        }
                    }
                }
                
                // 解析rehabilitationVideos JSON字符串为结构体
                List<AiWorkflowIssueResponse.RehabilitationVideo> parsedRehabVideos = new ArrayList<>();
                if (structData.getRehabilitationVideos() != null) {
                    for (String rehabVideoJson : structData.getRehabilitationVideos()) {
                        try {
                            AiWorkflowIssueResponse.RehabilitationVideo rehabVideo = objectMapper.readValue(rehabVideoJson, AiWorkflowIssueResponse.RehabilitationVideo.class);
                            parsedRehabVideos.add(rehabVideo);
                        } catch (Exception e) {
                            log.warn("解析rehabilitationVideo失败: {}, JSON: {}", e.getMessage(), rehabVideoJson);
                        }
                    }
                }
                
                diagnosisData.setPoseReference(parsedPoseReferences);
                diagnosisData.setRehabilitationVideos(parsedRehabVideos);
            }
            
            log.info("症状分析完成，诊断结果: {}, 风险等级: {}, 置信度: {}%, poseReference数量: {}, rehabilitationVideos数量: {}", 
                    diagnosisData.getPrimaryDiagnosis(), 
                    diagnosisData.getRiskLevel(), 
                    diagnosisData.getConfidence(),
                    diagnosisData.getPoseReference() != null ? diagnosisData.getPoseReference().size() : 0,
                    diagnosisData.getRehabilitationVideos() != null ? diagnosisData.getRehabilitationVideos().size() : 0);
            
            // 7. 将结果存储到数据库
            String username = request.getUsername() != null ? request.getUsername() : "anonymous_user";
            
            IssueAnalysisRecordDTO.CreateRequest createRequest = IssueAnalysisRecordDTO.CreateRequest.builder()
                    .username(username)
                    .sport(diagnosisData.getSport())
                    .posture(objectMapper.writeValueAsString(diagnosisData.getPosture()))
                    .riskLevel(diagnosisData.getRiskLevel())
                    .primaryDiagnosis(diagnosisData.getPrimaryDiagnosis())
                    .confidence(diagnosisData.getConfidence())
                    .isNormal(diagnosisData.isNormal())
                    .symptoms(objectMapper.writeValueAsString(diagnosisData.getSymptoms()))
                    .treatment(objectMapper.writeValueAsString(diagnosisData.getTreatment()))
                    .poseReference(objectMapper.writeValueAsString(diagnosisData.getPoseReference()))
                    .rehabilitationVideos(objectMapper.writeValueAsString(diagnosisData.getRehabilitationVideos()))
                    .build();
            
            ApiResponse<IssueAnalysisRecordDTO.ApiResponse> saveResponse =
                    issueAnalysisRecordService.createRecordWithParsedResults(createRequest);
            
            if (!"SUCCESS".equals(saveResponse.getResult())) {
                log.error("保存症状分析记录失败: {}", saveResponse.getMessage());
                return ResponseEntity.ok(ApiResponse.error("症状分析完成但保存失败: " + saveResponse.getMessage()));
            }

            log.info("症状分析完成并保存成功: recordId={}, 诊断结果: {}, 风险等级: {}",
                    saveResponse.getData().getId(), 
                    saveResponse.getData().getPrimaryDiagnosis(),
                    saveResponse.getData().getRiskLevel());

            return ResponseEntity.ok(ApiResponse.success("症状分析完成", saveResponse.getData()));
            
        } catch (Exception e) {
            log.error("姿态分析失败", e);
            return ResponseEntity.ok(ApiResponse.error("姿态分析失败: " + e.getMessage()));
        }
    }

}
