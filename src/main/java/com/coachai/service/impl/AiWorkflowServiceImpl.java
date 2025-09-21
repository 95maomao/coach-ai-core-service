package com.coachai.service.impl;

import com.coachai.config.AiWorkflowConfig;
import com.coachai.dto.AiWorkflowRequest;
import com.coachai.dto.AiWorkflowResponse;
import com.coachai.service.AiWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

/**
 * AI工作流服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiWorkflowServiceImpl implements AiWorkflowService {
    
    private final AiWorkflowConfig aiWorkflowConfig;
    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;
    
    @Override
    public AiWorkflowResponse callPoseAnalysisWorkflow(AiWorkflowRequest request) {
        try {
            log.info("开始调用AI工作流，用户: {}, 运动: {}, 姿势: {}", 
                    request.getParamJson().getUsername(),
                    request.getParamJson().getSport(),
                    request.getParamJson().getPosture());
            
            // 创建RestTemplate并设置超时
            RestTemplate restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofMillis(aiWorkflowConfig.getTimeout()))
                    .setReadTimeout(Duration.ofMillis(aiWorkflowConfig.getTimeout()))
                    .build();
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("ak", aiWorkflowConfig.getPoseAnalysis().getAk());
            
            // 创建请求实体
            HttpEntity<AiWorkflowRequest> requestEntity = new HttpEntity<>(request, headers);
            
            // 发送请求
            ResponseEntity<AiWorkflowResponse> responseEntity = restTemplate.exchange(
                    aiWorkflowConfig.getBaseUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    AiWorkflowResponse.class
            );
            
            AiWorkflowResponse response = responseEntity.getBody();
            if (response == null) {
                throw new RuntimeException("AI工作流返回空响应");
            }
            
            log.info("AI工作流调用成功，响应代码: {}, 成功: {}", response.getCode(), response.getSuccess());
            return response;
            
        } catch (Exception e) {
            log.error("调用AI工作流失败", e);
            throw new RuntimeException("调用AI工作流失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public AiWorkflowResponse.FinalMessage parseWorkflowResponse(AiWorkflowResponse response) {
        try {
            log.info("开始解析AI工作流响应");
            
            // 检查响应状态
            if (response == null || response.getData() == null || response.getData().getResult() == null) {
                throw new RuntimeException("AI工作流响应数据为空");
            }
            
            if (!Boolean.TRUE.equals(response.getSuccess()) || response.getCode() != 200) {
                throw new RuntimeException("AI工作流调用失败，代码: " + response.getCode() + ", 消息: " + response.getMessage());
            }
            
            // 第一层解析：从 data.result 中解析出 ParsedResult
            String resultJson = response.getData().getResult();
            log.debug("第一层JSON: {}", resultJson);
            
            AiWorkflowResponse.ParsedResult parsedResult = objectMapper.readValue(resultJson, AiWorkflowResponse.ParsedResult.class);
            
            if (parsedResult == null || parsedResult.getData() == null || parsedResult.getData().getStructData() == null) {
                throw new RuntimeException("解析第一层JSON失败，数据结构不正确");
            }
            
            // 第二层解析：从 structData.message 中解析出 FinalMessage
            String messageJson = parsedResult.getData().getStructData().getMessage();
            log.debug("第二层JSON: {}", messageJson);
            
            AiWorkflowResponse.FinalMessage finalMessage = objectMapper.readValue(messageJson, AiWorkflowResponse.FinalMessage.class);
            
            if (finalMessage == null) {
                throw new RuntimeException("解析第二层JSON失败，消息数据为空");
            }
            
            log.info("AI工作流响应解析成功，总体评分: {}, 分析结果数量: {}, 改进结果数量: {}", 
                    finalMessage.getOverallScore(),
                    finalMessage.getAnalysisResults() != null ? finalMessage.getAnalysisResults().size() : 0,
                    finalMessage.getImprovementResults() != null ? finalMessage.getImprovementResults().size() : 0);
            
            return finalMessage;
            
        } catch (Exception e) {
            log.error("解析AI工作流响应失败", e);
            throw new RuntimeException("解析AI工作流响应失败: " + e.getMessage(), e);
        }
    }
}
