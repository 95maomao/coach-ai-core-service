package com.coachai.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 症状分析分析响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueAnalysisResponse {
    /**
     * 角色
     */
    private String role;

    /**
     * 部分数据列表
     */
    private List<IssuePart> parts;

    /**
     * 姿态部分数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IssuePart {

        // 标准运动姿势参考
        private String poseReference;

        // 症状描述
        private String message;

        // 康复视频
        private String rehabilitationVideos;
    }
}
