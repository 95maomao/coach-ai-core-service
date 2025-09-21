package com.coachai.service.impl;

import com.coachai.common.ApiResponse;
import com.coachai.dto.PoseAnalysisRecordDTO;
import com.coachai.entity.PoseAnalysisRecord;
import com.coachai.repository.PoseAnalysisRecordRepository;
import com.coachai.service.PoseAnalysisRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

/**
 * 姿态分析记录服务实现类
 */
@Service
@Slf4j
public class PoseAnalysisRecordServiceImpl implements PoseAnalysisRecordService {

    @Autowired
    private PoseAnalysisRecordRepository poseAnalysisRecordRepository;

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
}
