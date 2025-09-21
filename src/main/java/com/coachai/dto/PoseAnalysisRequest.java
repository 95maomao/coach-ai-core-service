package com.coachai.dto;

import com.coachai.entity.CoachAiUser.PreferredSport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 姿态分析请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoseAnalysisRequest {
    
    /**
     * 图片链接
     */
    @NotBlank(message = "图片链接不能为空")
    private String imageLink;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 运动类型
     */
    @NotNull(message = "运动类型不能为空")
    private PreferredSport sport;
    
    /**
     * 姿势
     */
    @NotBlank(message = "姿势不能为空")
    private String posture;
}
