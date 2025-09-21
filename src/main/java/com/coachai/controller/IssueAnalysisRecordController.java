package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.config.AiWorkflowConfig;
import com.coachai.dto.*;
import com.coachai.service.AiWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    /**
     * 症状分析接口
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AiWorkflowIssueResponse.DiagnosisData>> processIssueAnalysis(
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
            
            log.info("症状分析完成，诊断结果: {}, 风险等级: {}, 置信度: {}%", 
                    diagnosisData.getPrimaryDiagnosis(), 
                    diagnosisData.getRiskLevel(), 
                    diagnosisData.getConfidence());
            
            // 6. 将结果存储到数据库
//            PoseAnalysisRecordDTO.CreateRequest createRequest = PoseAnalysisRecordDTO.CreateRequest.builder()
//                    .username(request.getUsername())
//                    .sport(request.getSport())
//                    .posture(request.getPosture())
//                    .userPoseImage(userPoseImage != null ? userPoseImage : request.getImageLink())
//                    .referencePoseImage(referencePoseImage != null ? referencePoseImage : request.getImageLink())
//                    .analysisResults(objectMapper.writeValueAsString(finalMessage.getAnalysisResults()))
//                    .improvementResults(objectMapper.writeValueAsString(finalMessage.getImprovementResults()))
//                    .build();
            
//            ApiResponse<PoseAnalysisRecordDTO.QueryResponse> saveResponse =
//                    poseAnalysisRecordService.createRecord(createRequest);
            
//            if (!"SUCCESS".equals(saveResponse.getResult())) {
//                log.error("保存姿态分析记录失败: {}", saveResponse.getMessage());
//                return ResponseEntity.ok(ApiResponse.error("姿态分析完成但保存失败: " + saveResponse.getMessage()));
//            }
//
//            log.info("姿态分析完成并保存成功: recordId={}, overallScore={}",
//                    saveResponse.getData().getId(), finalMessage.getOverallScore());
//
            return ResponseEntity.ok(ApiResponse.success("姿态分析完成", diagnosisData));
            
        } catch (Exception e) {
            log.error("姿态分析失败", e);
            return ResponseEntity.ok(ApiResponse.error("姿态分析失败: " + e.getMessage()));
        }
    }

}
