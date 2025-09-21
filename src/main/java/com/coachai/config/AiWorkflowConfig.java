package com.coachai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI工作流配置
 */
@Configuration
@ConfigurationProperties(prefix = "ai.workflow")
@Data
public class AiWorkflowConfig {
    
    /**
     * 基础URL
     */
    private String baseUrl;
    
    /**
     * 连接超时时间（毫秒）
     */
    private Long connectionTimeout = 15000L;  // 15秒
    
    /**
     * 读取超时时间（毫秒）
     */
    private Long readTimeout = 300000L;       // 5分钟
    
    /**
     * @deprecated 使用 connectionTimeout 和 readTimeout 替代
     */
    @Deprecated
    private Long timeout;
    
    /**
     * 姿态分析配置
     */
    private PoseAnalysis poseAnalysis;

    /**
     * 症状分析配置
     */
    private IssueAnalysis issueAnalysis;
    
    /**
     * 姿态分析配置内部类
     */
    @Data
    public static class PoseAnalysis {
        /**
         * API代码
         */
        private String apiCode;
        
        /**
         * 访问密钥
         */
        private String ak;
    }

    /**
     *  症状分析配置内部类
     */
    @Data
    public static class IssueAnalysis {
        /**
         * API代码
         */
        private String apiCode;

        /**
         * 访问密钥
         */
        private String ak;
    }
}
