package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.config.AiWorkflowConfig;
import com.coachai.dto.*;
import com.coachai.service.AiWorkflowService;
import com.coachai.service.PoseAnalysisRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 姿态分析记录Controller
 */
@RestController
@RequestMapping("/pose-analysis-records")
@RequiredArgsConstructor
@Slf4j
public class PoseAnalysisRecordController {

    private final PoseAnalysisRecordService poseAnalysisRecordService;
    private final AiWorkflowService aiWorkflowService;
    private final AiWorkflowConfig aiWorkflowConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

//    {
//        "imageLink": "https://guli-edu-ydw.oss-cn-beijing.aliyuncs.com/CoachAI/921.JPG",
//            "username": "weile",
//            "sport": "健身",
//            "posture": "深蹲"
//    }

    /**
     * 症状分析接口
     *
     * @param request 姿态分析请求，包含imageLink、username、sport、posture
     * @return 姿态分析结果
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<PoseAnalysisRecordDTO.ApiResponse>> processPoseAnalysis(
            @RequestBody @Valid PoseAnalysisRequest request) {
        
        log.info("接收到姿态分析请求: username={}, sport={}, posture={}, imageLink={}", 
                request.getUsername(), request.getSport(), request.getPosture(), request.getImageLink());
        
        try {
            // 1. 获取用户上一次的问题列表
            List<AiWorkflowRequest.LastProblem> lastProblems = 
                    poseAnalysisRecordService.getLastProblemsForUser(request.getUsername(), request.getPosture());

            log.info("获取用户上一次的问题列表: {}", lastProblems);

            // 2. 将lastProblems转换为前端需要的对象格式
            AiWorkflowRequest.LastProblemObject lastProblemObject = null;
            if (lastProblems != null && !lastProblems.isEmpty()) {
                List<String> problemStrings = lastProblems.stream()
                        .map(AiWorkflowRequest.LastProblem::getProblem)
                        .collect(java.util.stream.Collectors.toList());
                
                lastProblemObject = AiWorkflowRequest.LastProblemObject.builder()
                        .problem(problemStrings)
                        .build();
                
                log.info("转换后的lastProblem对象: {}", lastProblemObject);
            }
            
            // 3. 构建AI工作流请求
            AiWorkflowRequest.ParamJson paramJson = AiWorkflowRequest.ParamJson.builder()
                    .username(request.getUsername())
                    .sport(request.getSport())
                    .posture(request.getPosture())
                    .image(request.getImageLink())
                    .lastProblem(lastProblemObject)
                    .build();
            
            AiWorkflowRequest aiRequest = AiWorkflowRequest.builder()
                    .apiCode(aiWorkflowConfig.getPoseAnalysis().getApiCode())
                    .stream(false)
                    .paramJson(paramJson)
                    .build();
            
            // 4. 调用AI工作流
            log.info("开始调用AI工作流进行姿态分析");
            AiWorkflowResponse aiResponse = aiWorkflowService.callPoseAnalysisWorkflow(aiRequest);
            
            // 5. 解析AI工作流响应
            AiWorkflowResponse.FinalMessage finalMessage = aiWorkflowService.parseWorkflowResponse(aiResponse);
            
            // 6. 提取图片链接（从解析结果中获取）
            AiWorkflowResponse.ParsedResult parsedResult = objectMapper.readValue(
                    aiResponse.getData().getResult(), AiWorkflowResponse.ParsedResult.class);
            String userPoseImage = parsedResult.getData().getStructData().getUserPoseImage();
            String referencePoseImage = parsedResult.getData().getStructData().getReferencePoseImage();
            
            // 7. 将结果存储到数据库
            PoseAnalysisRecordDTO.CreateRequest createRequest = PoseAnalysisRecordDTO.CreateRequest.builder()
                    .username(request.getUsername())
                    .sport(request.getSport())
                    .posture(request.getPosture())
                    .userPoseImage(userPoseImage != null ? userPoseImage : request.getImageLink())
                    .referencePoseImage(referencePoseImage != null ? referencePoseImage : request.getImageLink())
                    .analysisResults(objectMapper.writeValueAsString(finalMessage.getAnalysisResults()))
                    .improvementResults(objectMapper.writeValueAsString(finalMessage.getImprovementResults()))
                    .build();
            
            ApiResponse<PoseAnalysisRecordDTO.ApiResponse> saveResponse = 
                    poseAnalysisRecordService.createRecordWithParsedResults(createRequest);
            
            if (!"SUCCESS".equals(saveResponse.getResult())) {
                log.error("保存姿态分析记录失败: {}", saveResponse.getMessage());
                return ResponseEntity.ok(ApiResponse.error("姿态分析完成但保存失败: " + saveResponse.getMessage()));
            }
            
            log.info("姿态分析完成并保存成功: recordId={}, overallScore={}, analysisResults数量={}, improvementResults数量={}", 
                    saveResponse.getData().getId(), finalMessage.getOverallScore(),
                    saveResponse.getData().getAnalysisResults().size(),
                    saveResponse.getData().getImprovementResults().size());
            saveResponse.getData().setOverallScore(finalMessage.getOverallScore());
            
            return ResponseEntity.ok(ApiResponse.success("姿态分析完成", saveResponse.getData()));
            
        } catch (Exception e) {
            log.error("姿态分析失败", e);
            return ResponseEntity.ok(ApiResponse.error("姿态分析失败: " + e.getMessage()));
        }
    }

    /**
     * 创建姿态分析记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PoseAnalysisRecordDTO.QueryResponse>> createRecord(
            @RequestBody @Valid PoseAnalysisRecordDTO.CreateRequest createRequest) {
        log.info("接收到创建姿态分析记录请求: username={}, posture={}", 
                createRequest.getUsername(), createRequest.getPosture());
        ApiResponse<PoseAnalysisRecordDTO.QueryResponse> response = poseAnalysisRecordService.createRecord(createRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户名和姿势查询最新的姿态分析记录
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<PoseAnalysisRecordDTO.QueryResponse>> getLatestRecord(
            @RequestParam String username, @RequestParam String posture) {
        log.info("接收到查询最新姿态分析记录请求: username={}, posture={}", username, posture);
        ApiResponse<PoseAnalysisRecordDTO.QueryResponse> response = 
                poseAnalysisRecordService.getLatestRecordByUsernameAndPosture(username, posture);
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "coach-ai-core-service");
        response.put("module", "Pose Analysis Record Management");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
