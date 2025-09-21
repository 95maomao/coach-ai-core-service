package com.coachai.repository;

import com.coachai.entity.IssueAnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 症状分析记录Repository接口
 */
@Repository
public interface IssueAnalysisRecordRepository extends JpaRepository<IssueAnalysisRecord, Long> {
    
    /**
     * 根据用户名查询最新的症状分析记录
     */
    @Query("SELECT r FROM IssueAnalysisRecord r WHERE r.username = :username ORDER BY r.createdAt DESC")
    List<IssueAnalysisRecord> findByUsernameOrderByCreatedAtDesc(@Param("username") String username);
    
    /**
     * 根据用户名和运动类型查询最新的症状分析记录
     */
    @Query("SELECT r FROM IssueAnalysisRecord r WHERE r.username = :username AND r.sport = :sport ORDER BY r.createdAt DESC")
    Optional<IssueAnalysisRecord> findLatestByUsernameAndSport(@Param("username") String username, @Param("sport") String sport);
    
    /**
     * 根据风险等级查询记录
     */
    List<IssueAnalysisRecord> findByRiskLevel(String riskLevel);
    
    /**
     * 查询异常记录（isNormal = false）
     */
    @Query("SELECT r FROM IssueAnalysisRecord r WHERE r.isNormal = false ORDER BY r.createdAt DESC")
    List<IssueAnalysisRecord> findAbnormalRecords();
    
    /**
     * 根据用户名和时间范围查询记录
     */
    @Query("SELECT r FROM IssueAnalysisRecord r WHERE r.username = :username AND r.createdAt BETWEEN :startTime AND :endTime ORDER BY r.createdAt DESC")
    List<IssueAnalysisRecord> findByUsernameAndTimeRange(@Param("username") String username, 
                                                         @Param("startTime") Long startTime, 
                                                         @Param("endTime") Long endTime);
}
