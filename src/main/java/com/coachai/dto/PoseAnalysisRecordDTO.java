package com.coachai.dto;

import com.coachai.entity.PoseAnalysisRecord;
import com.coachai.entity.CoachAiUser.PreferredSport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 姿态分析记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoseAnalysisRecordDTO {
    
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;
    
    private PreferredSport sport;
    
    @NotBlank(message = "姿势不能为空")
    private String posture;
    
    @NotBlank(message = "用户姿势图不能为空")
    private String userPoseImage;
    
    @NotBlank(message = "参考姿势图不能为空")
    private String referencePoseImage;
    
    private String analysisResults;
    
    private String improvementResults;
    
    private Long createdAt;
    private Long updatedAt;

    /**
     * 从实体类转换为DTO
     */
    public static PoseAnalysisRecordDTO fromEntity(PoseAnalysisRecord entity) {
        return PoseAnalysisRecordDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .sport(entity.getSport())
                .posture(entity.getPosture())
                .userPoseImage(entity.getUserPoseImage())
                .referencePoseImage(entity.getReferencePoseImage())
                .analysisResults(entity.getAnalysisResults())
                .improvementResults(entity.getImprovementResults())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 转换为实体类（用于创建）
     */
    public PoseAnalysisRecord toEntity() {
        return PoseAnalysisRecord.builder()
                .username(this.username)
                .sport(this.sport)
                .posture(this.posture)
                .userPoseImage(this.userPoseImage)
                .referencePoseImage(this.referencePoseImage)
                .analysisResults(this.analysisResults)
                .improvementResults(this.improvementResults)
                .build();
    }

    /**
     * 用于创建的简化DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50个字符")
        private String username;
        
        private PreferredSport sport;
        
        @NotBlank(message = "姿势不能为空")
        private String posture;
        
        @NotBlank(message = "用户姿势图不能为空")
        private String userPoseImage;
        
        @NotBlank(message = "参考姿势图不能为空")
        private String referencePoseImage;
        
        private String analysisResults;
        
        private String improvementResults;
    }

    /**
     * 用于查询响应的简化DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueryResponse {
        private Long id;
        private String username;
        private PreferredSport sport;
        private String posture;
        private String userPoseImage;
        private String referencePoseImage;
        private String analysisResults;
        private String improvementResults;
        private Long createdAt;
        private Long updatedAt;

        public static QueryResponse fromEntity(PoseAnalysisRecord entity) {
            return QueryResponse.builder()
                    .id(entity.getId())
                    .username(entity.getUsername())
                    .sport(entity.getSport())
                    .posture(entity.getPosture())
                    .userPoseImage(entity.getUserPoseImage())
                    .referencePoseImage(entity.getReferencePoseImage())
                    .analysisResults(entity.getAnalysisResults())
                    .improvementResults(entity.getImprovementResults())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }
}
