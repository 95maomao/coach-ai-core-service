package com.coachai.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 姿态分析请求DTO
 */
@Data
public class PoseAnalysisRequest {
    
    /**
     * JSON字符串参数（包含分析结果）
     */
    @NotBlank(message = "JSON字符串不能为空")
    private String jsonString;
    
    /**
     * Base64字符串参数（图片数据）
     */
    @NotBlank(message = "Base64字符串不能为空")
    private String base64String;
    
    /**
     * 类型参数（userPoseImage 或 referencePoseImage）
     */
    @NotBlank(message = "类型参数不能为空")
    private String type;
}
