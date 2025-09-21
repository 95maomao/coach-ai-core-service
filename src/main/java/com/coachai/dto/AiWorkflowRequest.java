package com.coachai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * AI工作流请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiWorkflowRequest {
    
    /**
     * API代码
     */
    @NotBlank(message = "API代码不能为空")
    private String apiCode;
    
    /**
     * 是否流式输出
     */
    @NotNull(message = "stream参数不能为空")
    private Boolean stream;
    
    /**
     * 参数JSON对象
     */
    @NotNull(message = "参数JSON不能为空")
    private ParamJson paramJson;
    
    /**
     * 参数JSON内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParamJson {
        /**
         * 用户名
         */
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        /**
         * 运动类型
         */
        @NotBlank(message = "运动类型不能为空")
        private String sport;
        
        /**
         * 姿势
         */
        @NotBlank(message = "姿势不能为空")
        private String posture;
        
        /**
         * 图片链接
         */
        @NotBlank(message = "图片链接不能为空")
        private String image;
        
        /**
         * 上一次的问题列表
         */
        private List<LastProblem> lastProblem;
    }
    
    /**
     * 上一次问题内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LastProblem {
        /**
         * 问题描述
         */
        private String problem;
        
        /**
         * 建议
         */
        private String suggestion;
    }
}
