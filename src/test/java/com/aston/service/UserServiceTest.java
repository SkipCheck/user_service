package com.aston.service;

import com.aston.dto.UserRequest;
import com.aston.dto.UserResponse;
import com.aston.entity.User;
import com.aston.exception.UserException;
import com.aston.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Юнит-тесты UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    private User testUser;
    private UserRequest testUserRequest;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);

        testUser = User.builder()
                .id(1L)
                .name("Тестовый пользователь")
                .email("test@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();

        testUserRequest = UserRequest.builder()
                .name("Тестовый пользователь")
                .email("test@example.com")
                .age(25)
                .build();
    }

    @Test
    @DisplayName("Создание пользователя - успех")
    void createUser_ValidData_ShouldCreateSuccessfully() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.createUser(testUserRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Получение пользователя по ID")
    void getUserById_ExistingId_ShouldReturnUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers_ShouldReturnAllUsers() {
        User user2 = User.builder()
                .id(2L)
                .name("Другой пользователь")
                .email("other@example.com")
                .age(30)
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(UserResponse::getEmail)
                .containsExactlyInAnyOrder("test@example.com", "other@example.com");

        verify(userRepository).findAll();
    }
}