package com.coachai.dto;

import lombok.Data;

/**
 * Base64图片请求DTO
 */
@Data
public class Base64ImageRequest {
    
    /**
     * base64编码的图片数据
     */
    private String base64Image;
    
    /**
     * 可选的文件名提示
     */
    private String fileName;
    
    /**
     * 可选的描述信息
     */
    private String description;
}
