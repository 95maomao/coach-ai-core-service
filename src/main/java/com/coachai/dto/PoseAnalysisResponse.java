package com.coachai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 姿态分析响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoseAnalysisResponse {
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 部分数据列表
     */
    private List<PosePart> parts;
    
    /**
     * 姿态部分数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PosePart {
        /**
         * 图片数据（base64）url
         */
        private String image;
        
        /**
         * 用户姿态图片指令（当type=userPoseImage时使用）
         */
        private String userPoseImageInstructions;
        
        /**
         * 参考姿态图片指令（当type=referencePoseImage时使用）
         */
        private String referencePoseImageInstructions;
    }
}
