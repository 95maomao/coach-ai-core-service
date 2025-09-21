package com.coachai.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 姿态分析记录实体类
 */
@Entity
@Table(name = "pose_analysis_record_flat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoseAnalysisRecord {
    
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

    @NotBlank(message = "姿势不能为空")
    @Column(nullable = false)
    private String posture;

    @NotBlank(message = "用户姿势图不能为空")
    @Column(name = "user_pose_image", nullable = false)
    private String userPoseImage;

    @NotBlank(message = "参考姿势图不能为空")
    @Column(name = "reference_pose_image", nullable = false)
    private String referencePoseImage;

    @Column(name = "analysis_results", columnDefinition = "JSON", nullable = false)
    private String analysisResults;

    @Column(name = "improvement_results", columnDefinition = "JSON")
    private String improvementResults;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
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
