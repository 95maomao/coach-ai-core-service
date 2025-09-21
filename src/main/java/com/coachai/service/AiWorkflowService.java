package com.coachai.service;

import com.coachai.dto.AiWorkflowIssueRequest;
import com.coachai.dto.AiWorkflowIssueResponse;
import com.coachai.dto.AiWorkflowRequest;
import com.coachai.dto.AiWorkflowResponse;

/**
 * AI工作流服务接口
 */
public interface AiWorkflowService {
    
    /**
     * 调用姿态分析工作流
     * 
     * @param request 工作流请求
     * @return 工作流响应
     */
    AiWorkflowResponse callPoseAnalysisWorkflow(AiWorkflowRequest request);
    
    /**
     * 解析AI工作流响应结果
     * 
     * @param response AI工作流响应
     * @return 解析后的最终消息
     */
    AiWorkflowResponse.FinalMessage parseWorkflowResponse(AiWorkflowResponse response);

    /**
     * 调用症状分析工作流
     *
     * @param request 工作流请求
     * @return 工作流响应
     */
    AiWorkflowIssueResponse callIssueAnalysisWorkflow(AiWorkflowIssueRequest request);

    /**
     * 解析症状分析工作流响应结果
     *
     * @param response AI工作流响应
     * @return 解析后的最终消息
     */
    AiWorkflowIssueResponse.StructData parseIssueWorkflowResponse(AiWorkflowIssueResponse response);
}
