package com.coachai.dto;

import com.coachai.entity.CoachAiUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import java.math.BigDecimal;

/**
 * CoachAI用户数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachAiUserDTO {
    
    private Long id;
    
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 50, message = "用户昵称长度不能超过50个字符")
    private String username;
    
    @Size(max = 60, message = "密码哈希长度不能超过60个字符")
    private String passwordHash;
    
    private CoachAiUser.PreferredSport preferredSport;
    
    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 255, message = "年龄不能大于255")
    private Integer age;
    
    @Min(value = 0, message = "身高不能小于0")
    @Max(value = 65535, message = "身高不能大于65535cm")
    private Integer height;
    
    @DecimalMin(value = "0.00", message = "体重不能小于0")
    @DecimalMax(value = "999.99", message = "体重不能大于999.99kg")
    private BigDecimal weight;
    
    private CoachAiUser.Gender gender;
    
    private Long createdAt;
    private Long updatedAt;

    /**
     * 从实体类转换为DTO
     */
    public static CoachAiUserDTO fromEntity(CoachAiUser entity) {
        return CoachAiUserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .passwordHash(entity.getPasswordHash())
                .preferredSport(entity.getPreferredSport())
                .age(entity.getAge())
                .height(entity.getHeight())
                .weight(entity.getWeight())
                .gender(entity.getGender())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 转换为实体类（用于创建）
     */
    public CoachAiUser toEntity() {
        return CoachAiUser.builder()
                .username(this.username)
                .passwordHash(this.passwordHash)
                .preferredSport(this.preferredSport)
                .age(this.age)
                .height(this.height)
                .weight(this.weight)
                .gender(this.gender != null ? this.gender : CoachAiUser.Gender.NOT_DISCLOSED)
                .build();
    }

    /**
     * 用于注册的简化DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "用户昵称不能为空")
        @Size(max = 50, message = "用户昵称长度不能超过50个字符")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
        private String password;
        
        private CoachAiUser.PreferredSport preferredSport;
        
        @Min(value = 0, message = "年龄不能小于0")
        @Max(value = 255, message = "年龄不能大于255")
        private Integer age;
        
        @Min(value = 0, message = "身高不能小于0")
        @Max(value = 65535, message = "身高不能大于65535cm")
        private Integer height;
        
        @DecimalMin(value = "0.00", message = "体重不能小于0")
        @DecimalMax(value = "999.99", message = "体重不能大于999.99kg")
        private BigDecimal weight;
        
        private CoachAiUser.Gender gender;
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
        private CoachAiUser.PreferredSport preferredSport;
        private Integer age;
        private Integer height;
        private BigDecimal weight;
        private CoachAiUser.Gender gender;
        private Long createdAt;
        private Long updatedAt;

        public static QueryResponse fromEntity(CoachAiUser entity) {
            return QueryResponse.builder()
                    .id(entity.getId())
                    .username(entity.getUsername())
                    .preferredSport(entity.getPreferredSport())
                    .age(entity.getAge())
                    .height(entity.getHeight())
                    .weight(entity.getWeight())
                    .gender(entity.getGender())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }
}
