package com.coachai.service;

import com.coachai.common.ApiResponse;
import com.coachai.dto.UserDTO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 创建用户
     */
    ApiResponse<UserDTO> createUser(UserDTO userDTO);
    
    /**
     * 根据ID获取用户
     */
    ApiResponse<UserDTO> getUserById(Long id);
    
    /**
     * 根据用户名获取用户
     */
    ApiResponse<UserDTO> getUserByUsername(String username);
    
    /**
     * 获取所有用户
     */
    ApiResponse<List<UserDTO>> getAllUsers();
    
    /**
     * 更新用户
     */
    ApiResponse<UserDTO> updateUser(Long id, UserDTO userDTO);
    
    /**
     * 删除用户
     */
    ApiResponse<Void> deleteUser(Long id);
}
