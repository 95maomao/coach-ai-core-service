package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.dto.PoseAnalysisRecordDTO;
import com.coachai.service.PoseAnalysisRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 姿态分析记录Controller
 */
@RestController
@RequestMapping("/pose-analysis-records")
@Slf4j
public class PoseAnalysisRecordController {

    @Autowired
    private PoseAnalysisRecordService poseAnalysisRecordService;

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
