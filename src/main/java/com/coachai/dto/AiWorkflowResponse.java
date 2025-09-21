package com.coachai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * AI工作流响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiWorkflowResponse {
    
    /**
     * 响应代码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应数据
     */
    private ResponseData data;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 响应数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResponseData {
        /**
         * 使用量
         */
        private Integer usage;
        
        /**
         * 使用量映射
         */
        private Object usageMap;
        
        /**
         * 结果（JSON字符串）
         */
        private String result;
    }
    
    /**
     * 解析后的结果数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParsedResult {
        /**
         * 数据
         */
        private ParsedData data;
        
        /**
         * 是否成功
         */
        private Boolean success;
        
        /**
         * 请求ID
         */
        private String requestId;
    }
    
    /**
     * 解析后的数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParsedData {
        /**
         * 消息列表
         */
        private List<Object> message;
        
        /**
         * 结构化数据
         */
        private StructData structData;
    }
    
    /**
     * 结构化数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StructData {
        /**
         * 用户姿势图片
         */
        private String userPoseImage;
        
        /**
         * 参考姿势图片
         */
        private String referencePoseImage;
        
        /**
         * 消息（FinalMessage对象）
         */
        private FinalMessage message;
    }
    
    /**
     * 最终解析的消息数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FinalMessage {
        /**
         * 是否成功
         */
        private Boolean success;
        
        /**
         * 总体评分
         */
        private Integer overallScore;
        
        /**
         * 分析结果列表
         */
        private List<AnalysisResult> analysisResults;
        
        /**
         * 改进结果列表
         */
        private List<ImprovementResult> improvementResults;
        
        /**
         * 用户姿势图片指令
         */
        private String userPoseImageInstructions;
    }
    
    /**
     * 分析结果内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalysisResult {
        /**
         * 问题描述
         */
        private String problem;
        
        /**
         * 建议
         */
        private String suggestion;
        
        /**
         * 是否是上次的问题
         */
        private Boolean isLastProblem;
    }
    
    /**
     * 改进结果内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImprovementResult {
        /**
         * 问题描述
         */
        private String problem;
        
        /**
         * 评估
         */
        private String evaluation;
    }
}
