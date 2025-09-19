package com.coachai.service.impl;

import com.coachai.common.ApiResponse;
import com.coachai.dto.UserDTO;
import com.coachai.entity.User;
import com.coachai.repository.UserRepository;
import com.coachai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<UserDTO> createUser(UserDTO userDTO) {
        log.info("开始创建用户: {}", userDTO.getUsername());
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            return ApiResponse.error("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return ApiResponse.error("邮箱已存在");
        }
        
        // 创建用户
        User user = userDTO.toEntity();
        User savedUser = userRepository.save(user);
        
        log.info("用户创建成功: {}", savedUser.getId());
        return ApiResponse.success("用户创建成功", UserDTO.fromEntity(savedUser));
    }

    @Override
    public ApiResponse<UserDTO> getUserById(Long id) {
        log.info("根据ID获取用户: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        return ApiResponse.success(UserDTO.fromEntity(user));
    }

    @Override
    public ApiResponse<UserDTO> getUserByUsername(String username) {
        log.info("根据用户名获取用户: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        return ApiResponse.success(UserDTO.fromEntity(user));
    }

    @Override
    public ApiResponse<List<UserDTO>> getAllUsers() {
        log.info("获取所有用户");
        
        List<UserDTO> users = userRepository.findAll()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        
        return ApiResponse.success(users);
    }

    @Override
    @Transactional
    public ApiResponse<UserDTO> updateUser(Long id, UserDTO userDTO) {
        log.info("更新用户: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        // 检查用户名是否被其他用户使用
        if (!existingUser.getUsername().equals(userDTO.getUsername()) 
                && userRepository.existsByUsername(userDTO.getUsername())) {
            return ApiResponse.error("用户名已被其他用户使用");
        }
        
        // 检查邮箱是否被其他用户使用
        if (!existingUser.getEmail().equals(userDTO.getEmail()) 
                && userRepository.existsByEmail(userDTO.getEmail())) {
            return ApiResponse.error("邮箱已被其他用户使用");
        }
        
        // 更新用户信息
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPassword(userDTO.getPassword());
        
        User updatedUser = userRepository.save(existingUser);
        
        log.info("用户更新成功: {}", updatedUser.getId());
        return ApiResponse.success("用户更新成功", UserDTO.fromEntity(updatedUser));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Long id) {
        log.info("删除用户: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        userRepository.delete(user);
        
        log.info("用户删除成功: {}", id);
        return ApiResponse.success("用户删除成功", null);
    }
}
