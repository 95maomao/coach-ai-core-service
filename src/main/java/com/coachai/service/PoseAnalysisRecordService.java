package com.coachai.service;

import com.coachai.common.ApiResponse;
import com.coachai.dto.PoseAnalysisRecordDTO;

/**
 * 姿态分析记录服务接口
 */
public interface PoseAnalysisRecordService {
    
    /**
     * 创建姿态分析记录
     */
    ApiResponse<PoseAnalysisRecordDTO.QueryResponse> createRecord(PoseAnalysisRecordDTO.CreateRequest createRequest);
    
    /**
     * 根据用户名和姿势查询最新记录
     */
    ApiResponse<PoseAnalysisRecordDTO.QueryResponse> getLatestRecordByUsernameAndPosture(String username, String posture);
}
