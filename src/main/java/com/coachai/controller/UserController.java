package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.dto.UserDTO;
import com.coachai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理Controller
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody @Valid UserDTO userDTO) {
        log.info("接收到创建用户请求: {}", userDTO.getUsername());
        ApiResponse<UserDTO> response = userService.createUser(userDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        log.info("接收到获取用户请求: {}", id);
        ApiResponse<UserDTO> response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户名获取用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        log.info("接收到根据用户名获取用户请求: {}", username);
        ApiResponse<UserDTO> response = userService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有用户
     */
    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<UserDTO>>> getAllUsers() {
        log.info("接收到获取所有用户请求");
        ApiResponse<java.util.List<UserDTO>> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody @Valid UserDTO userDTO) {
        log.info("接收到更新用户请求: {}", id);
        ApiResponse<UserDTO> response = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("接收到删除用户请求: {}", id);
        ApiResponse<Void> response = userService.deleteUser(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "coach-ai-core-service");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Hello接口
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, Coach AI Core Service!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
