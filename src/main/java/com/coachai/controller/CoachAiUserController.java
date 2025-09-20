package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.dto.CoachAiUserDTO;
import com.coachai.entity.CoachAiUser;
import com.coachai.service.CoachAiUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CoachAI用户管理Controller
 */
@RestController
@RequestMapping("/coach-ai-users")
@Slf4j
public class CoachAiUserController {

    @Autowired
    private CoachAiUserService coachAiUserService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CoachAiUserDTO.QueryResponse>> registerUser(
            @RequestBody @Valid CoachAiUserDTO.RegisterRequest registerRequest) {
        log.info("接收到用户注册请求: {}", registerRequest.getUsername());
        ApiResponse<CoachAiUserDTO.QueryResponse> response = coachAiUserService.registerUser(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CoachAiUserDTO.QueryResponse>> getUserById(@PathVariable Long id) {
        log.info("接收到根据ID查询用户请求: {}", id);
        ApiResponse<CoachAiUserDTO.QueryResponse> response = coachAiUserService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<CoachAiUserDTO.QueryResponse>> getUserByUsername(@PathVariable String username) {
        log.info("接收到根据用户名查询用户请求: {}", username);
        ApiResponse<CoachAiUserDTO.QueryResponse> response = coachAiUserService.getUserByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * 查询所有用户
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CoachAiUserDTO.QueryResponse>>> getAllUsers() {
        log.info("接收到查询所有用户请求");
        ApiResponse<List<CoachAiUserDTO.QueryResponse>> response = coachAiUserService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    /**
     * 根据年龄范围查询用户
     */
    @GetMapping("/age-range")
    public ResponseEntity<ApiResponse<List<CoachAiUserDTO.QueryResponse>>> getUsersByAgeRange(
            @RequestParam Integer minAge, @RequestParam Integer maxAge) {
        log.info("接收到根据年龄范围查询用户请求: {} - {}", minAge, maxAge);
        ApiResponse<List<CoachAiUserDTO.QueryResponse>> response = coachAiUserService.getUsersByAgeRange(minAge, maxAge);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据运动类型查询用户
     */
    @GetMapping("/sport/{sport}")
    public ResponseEntity<ApiResponse<List<CoachAiUserDTO.QueryResponse>>> getUsersByPreferredSport(
            @PathVariable CoachAiUser.PreferredSport sport) {
        log.info("接收到根据运动类型查询用户请求: {}", sport);
        ApiResponse<List<CoachAiUserDTO.QueryResponse>> response = coachAiUserService.getUsersByPreferredSport(sport);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据性别查询用户
     */
    @GetMapping("/gender/{gender}")
    public ResponseEntity<ApiResponse<List<CoachAiUserDTO.QueryResponse>>> getUsersByGender(
            @PathVariable CoachAiUser.Gender gender) {
        log.info("接收到根据性别查询用户请求: {}", gender);
        ApiResponse<List<CoachAiUserDTO.QueryResponse>> response = coachAiUserService.getUsersByGender(gender);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据用户名模糊查询
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CoachAiUserDTO.QueryResponse>>> searchUsersByUsername(
            @RequestParam String username) {
        log.info("接收到根据用户名模糊查询用户请求: {}", username);
        ApiResponse<List<CoachAiUserDTO.QueryResponse>> response = coachAiUserService.searchUsersByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CoachAiUserDTO.QueryResponse>> updateUser(
            @PathVariable Long id, @RequestBody @Valid CoachAiUserDTO userDTO) {
        log.info("接收到更新用户信息请求: {}", id);
        ApiResponse<CoachAiUserDTO.QueryResponse> response = coachAiUserService.updateUser(id, userDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("接收到删除用户请求: {}", id);
        ApiResponse<Void> response = coachAiUserService.deleteUser(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取运动类型枚举列表
     */
    @GetMapping("/sports")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPreferredSports() {
        log.info("接收到获取运动类型枚举列表请求");
        Map<String, String> sports = new HashMap<>();
        for (CoachAiUser.PreferredSport sport : CoachAiUser.PreferredSport.values()) {
            sports.put(sport.name(), sport.getDescription());
        }
        return ResponseEntity.ok(ApiResponse.success(sports));
    }

    /**
     * 获取性别枚举列表
     */
    @GetMapping("/genders")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGenders() {
        log.info("接收到获取性别枚举列表请求");
        Map<String, String> genders = new HashMap<>();
        for (CoachAiUser.Gender gender : CoachAiUser.Gender.values()) {
            genders.put(gender.name(), gender.getDescription());
        }
        return ResponseEntity.ok(ApiResponse.success(genders));
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "coach-ai-core-service");
        response.put("module", "CoachAI User Management");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
