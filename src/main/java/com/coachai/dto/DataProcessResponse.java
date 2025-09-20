package com.coachai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 数据处理响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataProcessResponse {
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 部分数据列表
     */
    private List<Part> parts;
    
    /**
     * 部分数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        /**
         * 图片数据（base64）
         */
        private String image;
        
        /**
         * 下一节点图片指令
         */
        private String nextNodeImageInstructions;
    }
}
