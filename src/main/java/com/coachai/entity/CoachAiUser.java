package com.coachai.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.DecimalMax;
import java.math.BigDecimal;

/**
 * CoachAI用户实体类
 */
@Entity
@Table(name = "coach_ai_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachAiUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 50, message = "用户昵称长度不能超过50个字符")
    @Column(unique = true, nullable = false)
    private String username;

    @Size(max = 60, message = "密码哈希长度不能超过60个字符")
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_sport")
    private PreferredSport preferredSport;

    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 255, message = "年龄不能大于255")
    private Integer age;

    @Min(value = 0, message = "身高不能小于0")
    @Max(value = 65535, message = "身高不能大于65535cm")
    private Integer height;

    @DecimalMin(value = "0.00", message = "体重不能小于0")
    @DecimalMax(value = "999.99", message = "体重不能大于999.99kg")
    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Gender gender = Gender.NOT_DISCLOSED;

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

    /**
     * 运动类型枚举
     */
    public enum PreferredSport {
        RUNNING("跑步"),
        SWIMMING("游泳"),
        CYCLING("骑行"),
        FITNESS("健身"),
        BASKETBALL("篮球"),
        FOOTBALL("足球"),
        TENNIS("网球"),
        BADMINTON("羽毛球"),
        YOGA("瑜伽"),
        DANCING("舞蹈");

        private final String description;

        PreferredSport(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 性别枚举
     */
    public enum Gender {
        MALE("男"),
        FEMALE("女"),
        OTHER("其他"),
        NOT_DISCLOSED("不愿透露");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
