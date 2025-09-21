package com.coachai.repository;

import com.coachai.entity.PoseAnalysisRecord;
import com.coachai.entity.CoachAiUser.PreferredSport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * 姿态分析记录数据访问层
 */
@Repository
public interface PoseAnalysisRecordRepository extends JpaRepository<PoseAnalysisRecord, Long> {
    
    /**
     * 根据用户名和姿势查询最新的一条记录
     */
    @Query("SELECT p FROM PoseAnalysisRecord p WHERE p.username = :username AND p.posture = :posture ORDER BY p.createdAt DESC")
    Optional<PoseAnalysisRecord> findLatestByUsernameAndPosture(@Param("username") String username, @Param("posture") String posture);
    
    /**
     * 根据用户名查询所有记录
     */
    List<PoseAnalysisRecord> findByUsernameOrderByCreatedAtDesc(String username);
    
    /**
     * 根据用户名和运动类型查询记录
     */
    List<PoseAnalysisRecord> findByUsernameAndSportOrderByCreatedAtDesc(String username, PreferredSport sport);
    
    /**
     * 根据运动类型查询记录
     */
    List<PoseAnalysisRecord> findBySportOrderByCreatedAtDesc(PreferredSport sport);
    
    /**
     * 根据姿势查询记录
     */
    List<PoseAnalysisRecord> findByPostureOrderByCreatedAtDesc(String posture);
    
    /**
     * 根据创建时间范围查询记录
     */
    @Query("SELECT p FROM PoseAnalysisRecord p WHERE p.createdAt BETWEEN :startTime AND :endTime ORDER BY p.createdAt DESC")
    List<PoseAnalysisRecord> findByCreatedAtBetween(@Param("startTime") Long startTime, @Param("endTime") Long endTime);
}
