package com.aston.controller;

import com.aston.dto.UserRequest;
import com.aston.dto.UserResponse;
import com.aston.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Тесты UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static UserService userServiceMock;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public UserService userService() {
            userServiceMock = mock(UserService.class);
            return userServiceMock;
        }
    }

    private UserRequest testUserRequest;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        testUserRequest = UserRequest.builder()
                .name("Тестовый пользователь")
                .email("test@example.com")
                .age(25)
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .name("Тестовый пользователь")
                .email("test@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Создание пользователя")
    void createUser_ShouldReturnCreated() throws Exception {
        when(userServiceMock.createUser(any(UserRequest.class))).thenReturn(testUserResponse);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Тестовый пользователь"));

        verify(userServiceMock).createUser(any(UserRequest.class));
    }

    @Test
    @DisplayName("Получение пользователя по ID")
    void getUserById_ShouldReturnUser() throws Exception {
        when(userServiceMock.getUserById(anyLong())).thenReturn(testUserResponse);

        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(userServiceMock).getUserById(1L);
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers_ShouldReturnUsers() throws Exception {
        List<UserResponse> users = Arrays.asList(testUserResponse);
        when(userServiceMock.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(userServiceMock).getAllUsers();
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void getUserByEmail_ShouldReturnUser() throws Exception {
        when(userServiceMock.getUserByEmail(anyString())).thenReturn(testUserResponse);

        mockMvc.perform(get("/api/v1/users/email/{email}", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userServiceMock).getUserByEmail("test@example.com");
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser_ShouldUpdateUser() throws Exception {
        when(userServiceMock.updateUser(anyLong(), any(UserRequest.class))).thenReturn(testUserResponse);

        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(userServiceMock).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser_ShouldDeleteUser() throws Exception {
        doNothing().when(userServiceMock).deleteUser(anyLong());

        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userServiceMock).deleteUser(1L);
    }

    @Test
    @DisplayName("Валидация данных - ошибка")
    void createUser_InvalidData_ShouldReturnBadRequest() throws Exception {
        UserRequest invalidRequest = UserRequest.builder()
                .name("")  // пустое имя
                .email("invalid-email")  // невалидный email
                .age(200)  // слишком большой возраст
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists());

        verify(userServiceMock, never()).createUser(any(UserRequest.class));
    }
}