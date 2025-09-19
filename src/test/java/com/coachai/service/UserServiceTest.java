package com.coachai.service;

import com.coachai.common.ApiResponse;
import com.coachai.dto.UserDTO;
import com.coachai.entity.User;
import com.coachai.repository.UserRepository;
import com.coachai.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试类
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO testUserDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateUser_Success() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        ApiResponse<UserDTO> response = userService.createUser(testUserDTO);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getResult());
        assertEquals("用户创建成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("testuser", response.getData().getUsername());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameExists() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        ApiResponse<UserDTO> response = userService.createUser(testUserDTO);

        // Then
        assertNotNull(response);
        assertEquals("ERROR", response.getResult());
        assertEquals("用户名已存在", response.getMessage());
        assertNull(response.getData());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        ApiResponse<UserDTO> response = userService.getUserById(1L);

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.getResult());
        assertNotNull(response.getData());
        assertEquals("testuser", response.getData().getUsername());

        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(javax.persistence.EntityNotFoundException.class, () -> {
            userService.getUserById(1L);
        });

        verify(userRepository).findById(1L);
    }
}
