package com.coachai.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 症状分析记录实体类
 */
@Entity
@Table(name = "issue_analysis_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueAnalysisRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    @Column(nullable = false)
    private String username;

    @NotBlank(message = "运动类型不能为空")
    @Column(nullable = false)
    private String sport;

    @Column(name = "posture", columnDefinition = "JSON", nullable = false)
    private String posture;

    @NotBlank(message = "风险等级不能为空")
    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @NotBlank(message = "主要诊断不能为空")
    @Column(name = "primary_diagnosis", nullable = false)
    private String primaryDiagnosis;

    @Min(value = 0, message = "置信度不能小于0")
    @Max(value = 100, message = "置信度不能大于100")
    @Column(nullable = false)
    private Integer confidence;

    @Column(name = "is_normal", nullable = false)
    private Boolean isNormal;

    @Column(name = "symptoms", columnDefinition = "JSON", nullable = false)
    private String symptoms;

    @Column(name = "treatment", columnDefinition = "JSON", nullable = false)
    private String treatment;

    @Column(name = "pose_reference", columnDefinition = "JSON")
    private String poseReference;

    @Column(name = "rehabilitation_videos", columnDefinition = "JSON")
    private String rehabilitationVideos;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @PrePersist
    protected void onCreate() {
        long now = System.currentTimeMillis();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}
