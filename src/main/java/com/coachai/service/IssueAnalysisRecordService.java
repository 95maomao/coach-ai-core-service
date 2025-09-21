package com.coachai.service;

import com.coachai.common.ApiResponse;
import com.coachai.dto.IssueAnalysisRecordDTO;

import java.util.List;

/**
 * 症状分析记录服务接口
 */
public interface IssueAnalysisRecordService {
    
    /**
     * 创建症状分析记录
     */
    ApiResponse<IssueAnalysisRecordDTO.QueryResponse> createRecord(IssueAnalysisRecordDTO.CreateRequest createRequest);
    
    /**
     * 创建症状分析记录并返回解析后的结构体响应
     */
    ApiResponse<IssueAnalysisRecordDTO.ApiResponse> createRecordWithParsedResults(IssueAnalysisRecordDTO.CreateRequest createRequest);
    
    /**
     * 根据用户名和运动类型查询最新记录
     */
    ApiResponse<IssueAnalysisRecordDTO.QueryResponse> getLatestRecordByUsernameAndSport(String username, String sport);
    
    /**
     * 根据用户名查询所有记录
     */
    ApiResponse<List<IssueAnalysisRecordDTO.QueryResponse>> getRecordsByUsername(String username);
    
    /**
     * 查询所有异常记录
     */
    ApiResponse<List<IssueAnalysisRecordDTO.QueryResponse>> getAbnormalRecords();
    
    /**
     * 根据风险等级查询记录
     */
    ApiResponse<List<IssueAnalysisRecordDTO.QueryResponse>> getRecordsByRiskLevel(String riskLevel);
}
