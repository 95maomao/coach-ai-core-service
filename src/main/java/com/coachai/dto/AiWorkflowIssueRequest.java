package com.coachai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class AiWorkflowIssueRequest {
    
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

        private List<String> bodyParts;

        private String sport;

        private List<String> posture;

        private String description;
    }

}
