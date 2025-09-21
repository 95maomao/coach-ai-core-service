package com.coachai.dto;

import com.coachai.entity.IssueAnalysisRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 症状分析记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueAnalysisRecordDTO {
    
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;
    
    @NotBlank(message = "运动类型不能为空")
    private String sport;
    
    private String posture; // JSON字符串格式
    
    @NotBlank(message = "风险等级不能为空")
    private String riskLevel;
    
    @NotBlank(message = "主要诊断不能为空")
    private String primaryDiagnosis;
    
    @Min(value = 0, message = "置信度不能小于0")
    @Max(value = 100, message = "置信度不能大于100")
    private Integer confidence;
    
    private Boolean isNormal;
    
    private String symptoms; // JSON字符串格式
    
    private String treatment; // JSON字符串格式
    
    private String poseReference; // JSON字符串格式
    
    private String rehabilitationVideos; // JSON字符串格式
    
    private Long createdAt;
    private Long updatedAt;

    /**
     * 从实体类转换为DTO
     */
    public static IssueAnalysisRecordDTO fromEntity(IssueAnalysisRecord entity) {
        return IssueAnalysisRecordDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .sport(entity.getSport())
                .posture(entity.getPosture())
                .riskLevel(entity.getRiskLevel())
                .primaryDiagnosis(entity.getPrimaryDiagnosis())
                .confidence(entity.getConfidence())
                .isNormal(entity.getIsNormal())
                .symptoms(entity.getSymptoms())
                .treatment(entity.getTreatment())
                .poseReference(entity.getPoseReference())
                .rehabilitationVideos(entity.getRehabilitationVideos())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
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
        
        @NotBlank(message = "运动类型不能为空")
        private String sport;
        
        private String posture; // JSON字符串
        
        @NotBlank(message = "风险等级不能为空")
        private String riskLevel;
        
        @NotBlank(message = "主要诊断不能为空")
        private String primaryDiagnosis;
        
        @Min(value = 0, message = "置信度不能小于0")
        @Max(value = 100, message = "置信度不能大于100")
        private Integer confidence;
        
        private Boolean isNormal;
        
        private String symptoms; // JSON字符串
        
        private String treatment; // JSON字符串
        
        private String poseReference; // JSON字符串
        
        private String rehabilitationVideos; // JSON字符串
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
        private String sport;
        private String posture; // JSON字符串
        private String riskLevel;
        private String primaryDiagnosis;
        private Integer confidence;
        private Boolean isNormal;
        private String symptoms; // JSON字符串
        private String treatment; // JSON字符串
        private String poseReference; // JSON字符串
        private String rehabilitationVideos; // JSON字符串
        private Long createdAt;
        private Long updatedAt;

        public static QueryResponse fromEntity(IssueAnalysisRecord entity) {
            return QueryResponse.builder()
                    .id(entity.getId())
                    .username(entity.getUsername())
                    .sport(entity.getSport())
                    .posture(entity.getPosture())
                    .riskLevel(entity.getRiskLevel())
                    .primaryDiagnosis(entity.getPrimaryDiagnosis())
                    .confidence(entity.getConfidence())
                    .isNormal(entity.getIsNormal())
                    .symptoms(entity.getSymptoms())
                    .treatment(entity.getTreatment())
                    .poseReference(entity.getPoseReference())
                    .rehabilitationVideos(entity.getRehabilitationVideos())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 用于API响应的DTO，包含解析后的结构体
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse {
        private Long id;
        private String username;
        private String sport;
        private List<String> posture;
        private String riskLevel;
        private String primaryDiagnosis;
        private Integer confidence;
        private Boolean isNormal;
        private List<AiWorkflowIssueResponse.Symptom> symptoms;
        private AiWorkflowIssueResponse.Treatment treatment;
        private List<AiWorkflowIssueResponse.PoseReference> poseReference;
        private List<AiWorkflowIssueResponse.RehabilitationVideo> rehabilitationVideos;
        private Long createdAt;
        private Long updatedAt;
    }
}
