package com.coachai.service.impl;

import com.coachai.common.ApiResponse;
import com.coachai.dto.AiWorkflowIssueResponse;
import com.coachai.dto.IssueAnalysisRecordDTO;
import com.coachai.entity.IssueAnalysisRecord;
import com.coachai.repository.IssueAnalysisRecordRepository;
import com.coachai.service.IssueAnalysisRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 症状分析记录服务实现类
 */
@Service
@Slf4j
public class IssueAnalysisRecordServiceImpl implements IssueAnalysisRecordService {

    @Autowired
    private IssueAnalysisRecordRepository issueAnalysisRecordRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public ApiResponse<IssueAnalysisRecordDTO.QueryResponse> createRecord(IssueAnalysisRecordDTO.CreateRequest createRequest) {
        log.info("开始创建症状分析记录: username={}, sport={}", createRequest.getUsername(), createRequest.getSport());
        
        try {
            // 创建记录实体
            IssueAnalysisRecord record = IssueAnalysisRecord.builder()
                    .username(createRequest.getUsername())
                    .sport(createRequest.getSport())
                    .posture(createRequest.getPosture())
                    .riskLevel(createRequest.getRiskLevel())
                    .primaryDiagnosis(createRequest.getPrimaryDiagnosis())
                    .confidence(createRequest.getConfidence())
                    .isNormal(createRequest.getIsNormal())
                    .symptoms(createRequest.getSymptoms())
                    .treatment(createRequest.getTreatment())
                    .poseReference(createRequest.getPoseReference())
                    .rehabilitationVideos(createRequest.getRehabilitationVideos())
                    .build();
            
            IssueAnalysisRecord savedRecord = issueAnalysisRecordRepository.save(record);
            
            log.info("症状分析记录创建成功: id={}", savedRecord.getId());
            return ApiResponse.success("症状分析记录创建成功", IssueAnalysisRecordDTO.QueryResponse.fromEntity(savedRecord));
            
        } catch (Exception e) {
            log.error("创建症状分析记录失败: username={}, sport={}, error={}", 
                    createRequest.getUsername(), createRequest.getSport(), e.getMessage(), e);
            return ApiResponse.error("创建症状分析记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<IssueAnalysisRecordDTO.ApiResponse> createRecordWithParsedResults(IssueAnalysisRecordDTO.CreateRequest createRequest) {
        log.info("开始创建症状分析记录并解析结果: username={}, sport={}", createRequest.getUsername(), createRequest.getSport());
        
        try {
            // 1. 先创建记录（保持JSON字符串格式）
            IssueAnalysisRecord record = IssueAnalysisRecord.builder()
                    .username(createRequest.getUsername())
                    .sport(createRequest.getSport())
                    .posture(createRequest.getPosture())
                    .riskLevel(createRequest.getRiskLevel())
                    .primaryDiagnosis(createRequest.getPrimaryDiagnosis())
                    .confidence(createRequest.getConfidence())
                    .isNormal(createRequest.getIsNormal())
                    .symptoms(createRequest.getSymptoms())
                    .treatment(createRequest.getTreatment())
                    .poseReference(createRequest.getPoseReference())
                    .rehabilitationVideos(createRequest.getRehabilitationVideos())
                    .build();
            
            IssueAnalysisRecord savedRecord = issueAnalysisRecordRepository.save(record);
            
            // 2. 解析JSON字符串为结构体
            List<String> parsedPosture = new ArrayList<>();
            List<AiWorkflowIssueResponse.Symptom> parsedSymptoms = new ArrayList<>();
            AiWorkflowIssueResponse.Treatment parsedTreatment = null;
            List<AiWorkflowIssueResponse.PoseReference> parsedPoseReferences = new ArrayList<>();
            List<AiWorkflowIssueResponse.RehabilitationVideo> parsedRehabVideos = new ArrayList<>();
            
            // 解析posture
            if (savedRecord.getPosture() != null && !savedRecord.getPosture().trim().isEmpty()) {
                try {
                    String[] postureArray = objectMapper.readValue(savedRecord.getPosture(), String[].class);
                    parsedPosture = List.of(postureArray);
                } catch (Exception e) {
                    log.warn("解析posture失败: {}", e.getMessage());
                }
            }
            
            // 解析symptoms
            if (savedRecord.getSymptoms() != null && !savedRecord.getSymptoms().trim().isEmpty()) {
                try {
                    AiWorkflowIssueResponse.Symptom[] symptomsArray = objectMapper.readValue(
                            savedRecord.getSymptoms(), AiWorkflowIssueResponse.Symptom[].class);
                    parsedSymptoms = List.of(symptomsArray);
                } catch (Exception e) {
                    log.warn("解析symptoms失败: {}", e.getMessage());
                }
            }
            
            // 解析treatment
            if (savedRecord.getTreatment() != null && !savedRecord.getTreatment().trim().isEmpty()) {
                try {
                    parsedTreatment = objectMapper.readValue(
                            savedRecord.getTreatment(), AiWorkflowIssueResponse.Treatment.class);
                } catch (Exception e) {
                    log.warn("解析treatment失败: {}", e.getMessage());
                }
            }
            
            // 解析poseReference
            if (savedRecord.getPoseReference() != null && !savedRecord.getPoseReference().trim().isEmpty()) {
                try {
                    AiWorkflowIssueResponse.PoseReference[] poseRefArray = objectMapper.readValue(
                            savedRecord.getPoseReference(), AiWorkflowIssueResponse.PoseReference[].class);
                    parsedPoseReferences = List.of(poseRefArray);
                } catch (Exception e) {
                    log.warn("解析poseReference失败: {}", e.getMessage());
                }
            }
            
            // 解析rehabilitationVideos
            if (savedRecord.getRehabilitationVideos() != null && !savedRecord.getRehabilitationVideos().trim().isEmpty()) {
                try {
                    AiWorkflowIssueResponse.RehabilitationVideo[] rehabVideoArray = objectMapper.readValue(
                            savedRecord.getRehabilitationVideos(), AiWorkflowIssueResponse.RehabilitationVideo[].class);
                    parsedRehabVideos = List.of(rehabVideoArray);
                } catch (Exception e) {
                    log.warn("解析rehabilitationVideos失败: {}", e.getMessage());
                }
            }
            
            // 3. 构建API响应
            IssueAnalysisRecordDTO.ApiResponse apiResponse = IssueAnalysisRecordDTO.ApiResponse.builder()
                    .id(savedRecord.getId())
                    .username(savedRecord.getUsername())
                    .sport(savedRecord.getSport())
                    .posture(parsedPosture)
                    .riskLevel(savedRecord.getRiskLevel())
                    .primaryDiagnosis(savedRecord.getPrimaryDiagnosis())
                    .confidence(savedRecord.getConfidence())
                    .isNormal(savedRecord.getIsNormal())
                    .symptoms(parsedSymptoms)
                    .treatment(parsedTreatment)
                    .poseReference(parsedPoseReferences)
                    .rehabilitationVideos(parsedRehabVideos)
                    .createdAt(savedRecord.getCreatedAt())
                    .updatedAt(savedRecord.getUpdatedAt())
                    .build();
            
            log.info("症状分析记录创建成功并解析完成: id={}, symptoms数量={}, poseReference数量={}, rehabilitationVideos数量={}", 
                    savedRecord.getId(), parsedSymptoms.size(), parsedPoseReferences.size(), parsedRehabVideos.size());
            
            return ApiResponse.success("症状分析记录创建成功", apiResponse);
            
        } catch (Exception e) {
            log.error("创建症状分析记录并解析结果失败: username={}, sport={}, error={}", 
                    createRequest.getUsername(), createRequest.getSport(), e.getMessage(), e);
            return ApiResponse.error("创建症状分析记录失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueAnalysisRecordDTO.QueryResponse> getLatestRecordByUsernameAndSport(String username, String sport) {
        log.info("查询最新症状分析记录: username={}, sport={}", username, sport);
        
        try {
            Optional<IssueAnalysisRecord> recordOpt = issueAnalysisRecordRepository.findLatestByUsernameAndSport(username, sport);
            
            if (recordOpt.isPresent()) {
                IssueAnalysisRecord record = recordOpt.get();
                log.info("找到最新症状分析记录: id={}, createdAt={}", record.getId(), record.getCreatedAt());
                return ApiResponse.success(IssueAnalysisRecordDTO.QueryResponse.fromEntity(record));
            } else {
                log.info("未找到匹配的症状分析记录: username={}, sport={}", username, sport);
                return ApiResponse.error("未找到匹配的症状分析记录");
            }
            
        } catch (Exception e) {
            log.error("查询症状分析记录失败: username={}, sport={}, error={}", 
                    username, sport, e.getMessage(), e);
            return ApiResponse.error("查询症状分析记录失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<IssueAnalysisRecordDTO.QueryResponse>> getRecordsByUsername(String username) {
        log.info("查询用户所有症状分析记录: username={}", username);
        
        try {
            List<IssueAnalysisRecord> records = issueAnalysisRecordRepository.findByUsernameOrderByCreatedAtDesc(username);
            
            List<IssueAnalysisRecordDTO.QueryResponse> responses = records.stream()
                    .map(IssueAnalysisRecordDTO.QueryResponse::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("查询用户症状分析记录成功: username={}, 记录数量={}", username, responses.size());
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("查询用户症状分析记录失败: username={}, error={}", username, e.getMessage(), e);
            return ApiResponse.error("查询用户症状分析记录失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<IssueAnalysisRecordDTO.QueryResponse>> getAbnormalRecords() {
        log.info("查询所有异常症状分析记录");
        
        try {
            List<IssueAnalysisRecord> records = issueAnalysisRecordRepository.findAbnormalRecords();
            
            List<IssueAnalysisRecordDTO.QueryResponse> responses = records.stream()
                    .map(IssueAnalysisRecordDTO.QueryResponse::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("查询异常症状分析记录成功: 记录数量={}", responses.size());
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("查询异常症状分析记录失败: error={}", e.getMessage(), e);
            return ApiResponse.error("查询异常症状分析记录失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<IssueAnalysisRecordDTO.QueryResponse>> getRecordsByRiskLevel(String riskLevel) {
        log.info("根据风险等级查询症状分析记录: riskLevel={}", riskLevel);
        
        try {
            List<IssueAnalysisRecord> records = issueAnalysisRecordRepository.findByRiskLevel(riskLevel);
            
            List<IssueAnalysisRecordDTO.QueryResponse> responses = records.stream()
                    .map(IssueAnalysisRecordDTO.QueryResponse::fromEntity)
                    .collect(Collectors.toList());
            
            log.info("根据风险等级查询症状分析记录成功: riskLevel={}, 记录数量={}", riskLevel, responses.size());
            return ApiResponse.success(responses);
            
        } catch (Exception e) {
            log.error("根据风险等级查询症状分析记录失败: riskLevel={}, error={}", riskLevel, e.getMessage(), e);
            return ApiResponse.error("根据风险等级查询症状分析记录失败: " + e.getMessage());
        }
    }
}
