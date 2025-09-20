package com.coachai.repository;

import com.coachai.entity.CoachAiUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * CoachAI用户数据访问层
 */
@Repository
public interface CoachAiUserRepository extends JpaRepository<CoachAiUser, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<CoachAiUser> findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 根据年龄范围查找用户
     */
    List<CoachAiUser> findByAgeBetween(Integer minAge, Integer maxAge);
    
    /**
     * 根据运动类型查找用户
     */
    List<CoachAiUser> findByPreferredSport(CoachAiUser.PreferredSport preferredSport);
    
    /**
     * 根据性别查找用户
     */
    List<CoachAiUser> findByGender(CoachAiUser.Gender gender);
    
    /**
     * 根据创建时间范围查找用户
     */
    @Query("SELECT u FROM CoachAiUser u WHERE u.createdAt BETWEEN :startTime AND :endTime")
    List<CoachAiUser> findByCreatedAtBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);
    
    /**
     * 根据用户名模糊查询
     */
    List<CoachAiUser> findByUsernameContaining(String username);
}
