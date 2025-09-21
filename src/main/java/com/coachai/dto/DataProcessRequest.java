package com.coachai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 数据处理请求DTO
 */
@Data
public class DataProcessRequest {
    
    /**
     * JSON字符串参数
     */
    @NotBlank(message = "JSON字符串不能为空")
    private String jsonString;
    
    /**
     * Base64字符串参数
     */
    @NotBlank(message = "link不能为空")
    private String base64String;
}
