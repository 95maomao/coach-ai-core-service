package com.coachai.service;

import com.coachai.common.ApiResponse;
import com.coachai.dto.AiWorkflowRequest;
import com.coachai.dto.PoseAnalysisRecordDTO;

import java.util.List;

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
    
    /**
     * 获取用户指定姿势的上一次问题列表（用于AI工作流的lastProblem参数）
     */
    List<AiWorkflowRequest.LastProblem> getLastProblemsForUser(String username, String posture);

    /**
     * 创建姿态分析记录并返回解析后的结构体响应
     */
    ApiResponse<PoseAnalysisRecordDTO.ApiResponse> createRecordWithParsedResults(PoseAnalysisRecordDTO.CreateRequest createRequest);
}
