package com.coachai.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 姿态分析出书处理请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoseAnalysisDataProcessRequest {

    /**
     * JSON字符串参数（包含分析结果）
     */
    @NotBlank(message = "JSON字符串不能为空")
    private String jsonString;

    /**
     * 图片link
     */
    @NotBlank(message = "图片链接不能为空")
    private String imageUrl;

    /**
     * 类型参数（userPoseImage 或 referencePoseImage）
     */
    @NotBlank(message = "类型参数不能为空")
    private String type;

}
