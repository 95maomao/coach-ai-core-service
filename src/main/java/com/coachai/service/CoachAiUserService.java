package com.coachai.service;

import com.coachai.common.ApiResponse;
import com.coachai.dto.CoachAiUserDTO;
import com.coachai.entity.CoachAiUser;

import java.util.List;

/**
 * CoachAI用户服务接口
 */
public interface CoachAiUserService {
    
    /**
     * 用户注册
     */
    ApiResponse<CoachAiUserDTO.QueryResponse> registerUser(CoachAiUserDTO.RegisterRequest registerRequest);
    
    /**
     * 根据ID查询用户
     */
    ApiResponse<CoachAiUserDTO.QueryResponse> getUserById(Long id);
    
    /**
     * 根据用户名查询用户
     */
    ApiResponse<CoachAiUserDTO.QueryResponse> getUserByUsername(String username);
    
    /**
     * 查询所有用户
     */
    ApiResponse<List<CoachAiUserDTO.QueryResponse>> getAllUsers();
    
    /**
     * 根据年龄范围查询用户
     */
    ApiResponse<List<CoachAiUserDTO.QueryResponse>> getUsersByAgeRange(Integer minAge, Integer maxAge);
    
    /**
     * 根据运动类型查询用户
     */
    ApiResponse<List<CoachAiUserDTO.QueryResponse>> getUsersByPreferredSport(CoachAiUser.PreferredSport preferredSport);
    
    /**
     * 根据性别查询用户
     */
    ApiResponse<List<CoachAiUserDTO.QueryResponse>> getUsersByGender(CoachAiUser.Gender gender);
    
    /**
     * 根据用户名模糊查询
     */
    ApiResponse<List<CoachAiUserDTO.QueryResponse>> searchUsersByUsername(String username);
    
    /**
     * 更新用户信息
     */
    ApiResponse<CoachAiUserDTO.QueryResponse> updateUser(Long id, CoachAiUserDTO userDTO);
    
    /**
     * 删除用户
     */
    ApiResponse<Void> deleteUser(Long id);
    
    /**
     * 用户登录
     */
    ApiResponse<CoachAiUserDTO.LoginResponse> loginUser(CoachAiUserDTO.LoginRequest loginRequest);
    
    /**
     * 根据用户ID获取用户名
     */
    ApiResponse<CoachAiUserDTO.UsernameResponse> getUsernameById(Long id);
}
