package com.coachai.service.impl;

import com.coachai.common.ApiResponse;
import com.coachai.dto.CoachAiUserDTO;
import com.coachai.entity.CoachAiUser;
import com.coachai.repository.CoachAiUserRepository;
import com.coachai.service.CoachAiUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CoachAI用户服务实现类
 */
@Service
@Slf4j
public class CoachAiUserServiceImpl implements CoachAiUserService {

    @Autowired
    private CoachAiUserRepository coachAiUserRepository;

    /**
     * 将密码转换为Base64编码
     */
    private String encodePassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    /**
     * 验证密码
     */
    private boolean verifyPassword(String inputPassword, String storedPasswordHash) {
        if (inputPassword == null || storedPasswordHash == null) {
            return false;
        }
        String encodedInput = Base64.getEncoder().encodeToString(inputPassword.getBytes());
        return encodedInput.equals(storedPasswordHash);
    }

    @Override
    @Transactional
    public ApiResponse<CoachAiUserDTO.QueryResponse> registerUser(CoachAiUserDTO.RegisterRequest registerRequest) {
        log.info("开始注册用户: {}", registerRequest.getUsername());
        
        // 检查用户名是否已存在
        if (coachAiUserRepository.existsByUsername(registerRequest.getUsername())) {
            return ApiResponse.error("用户名已存在");
        }
        
        // 创建用户实体
        CoachAiUser user = CoachAiUser.builder()
                .username(registerRequest.getUsername())
                .passwordHash(encodePassword(registerRequest.getPassword()))
                .preferredSport(registerRequest.getPreferredSport())
                .age(registerRequest.getAge())
                .height(registerRequest.getHeight())
                .weight(registerRequest.getWeight())
                .gender(registerRequest.getGender() != null ? registerRequest.getGender() : CoachAiUser.Gender.NOT_DISCLOSED)
                .build();
        
        CoachAiUser savedUser = coachAiUserRepository.save(user);
        
        log.info("用户注册成功: {}", savedUser.getId());
        return ApiResponse.success("用户注册成功", CoachAiUserDTO.QueryResponse.fromEntity(savedUser));
    }

    @Override
    public ApiResponse<CoachAiUserDTO.QueryResponse> getUserById(Long id) {
        log.info("根据ID查询用户: {}", id);
        
        CoachAiUser user = coachAiUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        return ApiResponse.success(CoachAiUserDTO.QueryResponse.fromEntity(user));
    }

    @Override
    public ApiResponse<CoachAiUserDTO.QueryResponse> getUserByUsername(String username) {
        log.info("根据用户名查询用户: {}", username);
        
        CoachAiUser user = coachAiUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        return ApiResponse.success(CoachAiUserDTO.QueryResponse.fromEntity(user));
    }

    @Override
    public ApiResponse<List<CoachAiUserDTO.QueryResponse>> getAllUsers() {
        log.info("查询所有用户");
        
        List<CoachAiUserDTO.QueryResponse> users = coachAiUserRepository.findAll()
                .stream()
                .map(CoachAiUserDTO.QueryResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ApiResponse.success(users);
    }

    @Override
    public ApiResponse<List<CoachAiUserDTO.QueryResponse>> getUsersByAgeRange(Integer minAge, Integer maxAge) {
        log.info("根据年龄范围查询用户: {} - {}", minAge, maxAge);
        
        List<CoachAiUserDTO.QueryResponse> users = coachAiUserRepository.findByAgeBetween(minAge, maxAge)
                .stream()
                .map(CoachAiUserDTO.QueryResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ApiResponse.success(users);
    }

    @Override
    public ApiResponse<List<CoachAiUserDTO.QueryResponse>> getUsersByPreferredSport(CoachAiUser.PreferredSport preferredSport) {
        log.info("根据运动类型查询用户: {}", preferredSport);
        
        List<CoachAiUserDTO.QueryResponse> users = coachAiUserRepository.findByPreferredSport(preferredSport)
                .stream()
                .map(CoachAiUserDTO.QueryResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ApiResponse.success(users);
    }

    @Override
    public ApiResponse<List<CoachAiUserDTO.QueryResponse>> getUsersByGender(CoachAiUser.Gender gender) {
        log.info("根据性别查询用户: {}", gender);
        
        List<CoachAiUserDTO.QueryResponse> users = coachAiUserRepository.findByGender(gender)
                .stream()
                .map(CoachAiUserDTO.QueryResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ApiResponse.success(users);
    }

    @Override
    public ApiResponse<List<CoachAiUserDTO.QueryResponse>> searchUsersByUsername(String username) {
        log.info("根据用户名模糊查询用户: {}", username);
        
        List<CoachAiUserDTO.QueryResponse> users = coachAiUserRepository.findByUsernameContaining(username)
                .stream()
                .map(CoachAiUserDTO.QueryResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ApiResponse.success(users);
    }

    @Override
    @Transactional
    public ApiResponse<CoachAiUserDTO.QueryResponse> updateUser(Long id, CoachAiUserDTO userDTO) {
        log.info("更新用户信息: {}", id);
        
        CoachAiUser existingUser = coachAiUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        // 检查用户名是否被其他用户使用
        if (!existingUser.getUsername().equals(userDTO.getUsername()) 
                && coachAiUserRepository.existsByUsername(userDTO.getUsername())) {
            return ApiResponse.error("用户名已被其他用户使用");
        }
        
        // 更新用户信息
        existingUser.setUsername(userDTO.getUsername());
        if (userDTO.getPasswordHash() != null && !userDTO.getPasswordHash().isEmpty()) {
            existingUser.setPasswordHash(userDTO.getPasswordHash());
        }
        existingUser.setPreferredSport(userDTO.getPreferredSport());
        existingUser.setAge(userDTO.getAge());
        existingUser.setHeight(userDTO.getHeight());
        existingUser.setWeight(userDTO.getWeight());
        existingUser.setGender(userDTO.getGender());
        
        CoachAiUser updatedUser = coachAiUserRepository.save(existingUser);
        
        log.info("用户信息更新成功: {}", updatedUser.getId());
        return ApiResponse.success("用户信息更新成功", CoachAiUserDTO.QueryResponse.fromEntity(updatedUser));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Long id) {
        log.info("删除用户: {}", id);
        
        CoachAiUser user = coachAiUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        coachAiUserRepository.delete(user);
        
        log.info("用户删除成功: {}", id);
        return ApiResponse.success("用户删除成功", null);
    }

    @Override
    public ApiResponse<CoachAiUserDTO.LoginResponse> loginUser(CoachAiUserDTO.LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        
        // 查找用户
        CoachAiUser user = coachAiUserRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);
        
        if (user == null) {
            return ApiResponse.error("用户名或密码错误");
        }
        
        // 验证密码
        if (!verifyPassword(loginRequest.getPassword(), user.getPasswordHash())) {
            return ApiResponse.error("用户名或密码错误");
        }
        
        // 构建登录响应
        CoachAiUserDTO.LoginResponse loginResponse = CoachAiUserDTO.LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .message("登录成功")
                .loginTime(System.currentTimeMillis())
                .build();
        
        log.info("用户登录成功: {}", user.getUsername());
        return ApiResponse.success("登录成功", loginResponse);
    }

    @Override
    public ApiResponse<CoachAiUserDTO.UsernameResponse> getUsernameById(Long id) {
        log.info("根据ID查询用户名: {}", id);
        
        CoachAiUser user = coachAiUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        
        CoachAiUserDTO.UsernameResponse response = CoachAiUserDTO.UsernameResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .build();
        
        return ApiResponse.success(response);
    }
}
