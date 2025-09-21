package com.coachai.service.impl;

import com.coachai.common.ApiResponse;
import com.coachai.dto.AiWorkflowRequest;
import com.coachai.dto.AiWorkflowResponse;
import com.coachai.dto.PoseAnalysisRecordDTO;
import com.coachai.entity.PoseAnalysisRecord;
import com.coachai.repository.PoseAnalysisRecordRepository;
import com.coachai.service.PoseAnalysisRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 姿态分析记录服务实现类
 */
@Service
@Slf4j
public class PoseAnalysisRecordServiceImpl implements PoseAnalysisRecordService {

    @Autowired
    private PoseAnalysisRecordRepository poseAnalysisRecordRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApiResponse<PoseAnalysisRecordDTO.QueryResponse> createRecord(PoseAnalysisRecordDTO.CreateRequest createRequest) {
        log.info("开始创建姿态分析记录: username={}, posture={}", createRequest.getUsername(), createRequest.getPosture());
        
        try {
            // 创建记录实体
            PoseAnalysisRecord record = PoseAnalysisRecord.builder()
                    .username(createRequest.getUsername())
                    .sport(createRequest.getSport())
                    .posture(createRequest.getPosture())
                    .userPoseImage(createRequest.getUserPoseImage())
                    .referencePoseImage(createRequest.getReferencePoseImage())
                    .analysisResults(createRequest.getAnalysisResults())
                    .improvementResults(createRequest.getImprovementResults())
                    .build();
            
            PoseAnalysisRecord savedRecord = poseAnalysisRecordRepository.save(record);
            
            log.info("姿态分析记录创建成功: id={}", savedRecord.getId());
            return ApiResponse.success("姿态分析记录创建成功", PoseAnalysisRecordDTO.QueryResponse.fromEntity(savedRecord));
            
        } catch (Exception e) {
            log.error("创建姿态分析记录失败: username={}, posture={}, error={}", 
                    createRequest.getUsername(), createRequest.getPosture(), e.getMessage(), e);
            return ApiResponse.error("创建姿态分析记录失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PoseAnalysisRecordDTO.QueryResponse> getLatestRecordByUsernameAndPosture(String username, String posture) {
        log.info("查询最新姿态分析记录: username={}, posture={}", username, posture);
        
        try {
            Optional<PoseAnalysisRecord> recordOpt = poseAnalysisRecordRepository.findLatestByUsernameAndPosture(username, posture);
            
            if (recordOpt.isPresent()) {
                PoseAnalysisRecord record = recordOpt.get();
                log.info("找到最新姿态分析记录: id={}, createdAt={}", record.getId(), record.getCreatedAt());
                return ApiResponse.success(PoseAnalysisRecordDTO.QueryResponse.fromEntity(record));
            } else {
                log.info("未找到匹配的姿态分析记录: username={}, posture={}", username, posture);
                return ApiResponse.error("未找到匹配的姿态分析记录");
            }
            
        } catch (Exception e) {
            log.error("查询姿态分析记录失败: username={}, posture={}, error={}", 
                    username, posture, e.getMessage(), e);
            return ApiResponse.error("查询姿态分析记录失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<AiWorkflowRequest.LastProblem> getLastProblemsForUser(String username, String posture) {
        log.info("获取用户上一次问题列表: username={}, posture={}", username, posture);
        
        try {
            Optional<PoseAnalysisRecord> recordOpt = poseAnalysisRecordRepository.findLatestByUsernameAndPosture(username, posture);
            
            if (!recordOpt.isPresent()) {
                log.info("未找到历史记录，返回空的问题列表: username={}, posture={}", username, posture);
                return Collections.emptyList();
            }
            
            PoseAnalysisRecord record = recordOpt.get();
            String analysisResultsJson = record.getAnalysisResults();
            
            if (analysisResultsJson == null || analysisResultsJson.trim().isEmpty()) {
                log.info("历史记录中没有分析结果，返回空的问题列表: recordId={}", record.getId());
                return Collections.emptyList();
            }
            
            // 解析analysisResults JSON，提取问题和建议
            AiWorkflowResponse.AnalysisResult[] analysisResults = objectMapper.readValue(
                    analysisResultsJson, AiWorkflowResponse.AnalysisResult[].class);
            
            List<AiWorkflowRequest.LastProblem> lastProblems = new ArrayList<>();
            for (AiWorkflowResponse.AnalysisResult result : analysisResults) {
                AiWorkflowRequest.LastProblem lastProblem = AiWorkflowRequest.LastProblem.builder()
                        .problem(result.getProblem())
                        .suggestion(result.getSuggestion())
                        .build();
                lastProblems.add(lastProblem);
            }
            
            log.info("成功获取用户上一次问题列表: username={}, posture={}, 问题数量={}", 
                    username, posture, lastProblems.size());
            return lastProblems;
            
        } catch (Exception e) {
            log.error("获取用户上一次问题列表失败: username={}, posture={}, error={}", 
                    username, posture, e.getMessage(), e);
            // 出错时返回空列表，不影响主流程
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public ApiResponse<PoseAnalysisRecordDTO.ApiResponse> createRecordWithParsedResults(PoseAnalysisRecordDTO.CreateRequest createRequest) {
        log.info("开始创建姿态分析记录并解析结果: username={}, posture={}", createRequest.getUsername(), createRequest.getPosture());
        
        try {
            // 1. 先创建记录（保持JSON字符串格式）
            PoseAnalysisRecord record = PoseAnalysisRecord.builder()
                    .username(createRequest.getUsername())
                    .sport(createRequest.getSport())
                    .posture(createRequest.getPosture())
                    .userPoseImage(createRequest.getUserPoseImage())
                    .referencePoseImage(createRequest.getReferencePoseImage())
                    .analysisResults(createRequest.getAnalysisResults())
                    .improvementResults(createRequest.getImprovementResults())
                    .build();
            
            PoseAnalysisRecord savedRecord = poseAnalysisRecordRepository.save(record);
            
            // 2. 解析JSON字符串为结构体
            List<AiWorkflowResponse.AnalysisResult> parsedAnalysisResults = new ArrayList<>();
            List<AiWorkflowResponse.ImprovementResult> parsedImprovementResults = new ArrayList<>();
            
            // 解析analysisResults
            if (savedRecord.getAnalysisResults() != null && !savedRecord.getAnalysisResults().trim().isEmpty()) {
                try {
                    AiWorkflowResponse.AnalysisResult[] analysisArray = objectMapper.readValue(
                            savedRecord.getAnalysisResults(), AiWorkflowResponse.AnalysisResult[].class);
                    parsedAnalysisResults = List.of(analysisArray);
                } catch (Exception e) {
                    log.warn("解析analysisResults失败: {}", e.getMessage());
                }
            }
            
            // 解析improvementResults
            if (savedRecord.getImprovementResults() != null && !savedRecord.getImprovementResults().trim().isEmpty()) {
                try {
                    AiWorkflowResponse.ImprovementResult[] improvementArray = objectMapper.readValue(
                            savedRecord.getImprovementResults(), AiWorkflowResponse.ImprovementResult[].class);
                    parsedImprovementResults = List.of(improvementArray);
                } catch (Exception e) {
                    log.warn("解析improvementResults失败: {}", e.getMessage());
                }
            }
            
            // 3. 构建API响应
            PoseAnalysisRecordDTO.ApiResponse apiResponse = PoseAnalysisRecordDTO.ApiResponse.builder()
                    .id(savedRecord.getId())
                    .username(savedRecord.getUsername())
                    .sport(savedRecord.getSport())
                    .posture(savedRecord.getPosture())
                    .userPoseImage(savedRecord.getUserPoseImage())
                    .referencePoseImage(savedRecord.getReferencePoseImage())
                    .analysisResults(parsedAnalysisResults)
                    .improvementResults(parsedImprovementResults)
                    .createdAt(savedRecord.getCreatedAt())
                    .updatedAt(savedRecord.getUpdatedAt())
                    .build();
            
            log.info("姿态分析记录创建成功并解析完成: id={}, analysisResults数量={}, improvementResults数量={}", 
                    savedRecord.getId(), parsedAnalysisResults.size(), parsedImprovementResults.size());
            
            return ApiResponse.success("姿态分析记录创建成功", apiResponse);
            
        } catch (Exception e) {
            log.error("创建姿态分析记录并解析结果失败: username={}, posture={}, error={}", 
                    createRequest.getUsername(), createRequest.getPosture(), e.getMessage(), e);
            return ApiResponse.error("创建姿态分析记录失败: " + e.getMessage());
        }
    }
}
