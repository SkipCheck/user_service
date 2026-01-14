package com.aston.repository;

import com.aston.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Интеграционные тесты UserRepository")
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("Тестовый пользователь")
                .email("test@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Сохранение пользователя")
    void save_ShouldSaveUser() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Поиск по ID")
    void findById_ShouldReturnUser() {
        User savedUser = userRepository.save(testUser);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Поиск по email")
    void findByEmail_ShouldReturnUser() {
        userRepository.save(testUser);
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Тестовый пользователь");
    }

    @Test
    @DisplayName("Проверка уникальности email")
    void save_DuplicateEmail_ShouldThrowException() {
        userRepository.save(testUser);

        User duplicateUser = User.builder()
                .name("Другой пользователь")
                .email("test@example.com")
                .age(30)
                .build();

        assertThatThrownBy(() -> userRepository.save(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Поиск по имени")
    void findByNameContainingIgnoreCase_ShouldReturnUsers() {
        User user1 = User.builder().name("Иван Иванов").email("ivan@test.com").age(25).build();
        User user2 = User.builder().name("Иван Петров").email("ivan2@test.com").age(30).build();
        User user3 = User.builder().name("Петр Сидоров").email("petr@test.com").age(35).build();

        userRepository.saveAll(List.of(user1, user2, user3));

        List<User> users = userRepository.findByNameContainingIgnoreCase("иван");

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("ivan@test.com", "ivan2@test.com");
    }

    @Test
    @DisplayName("Проверка существования email")
    void existsByEmail_ShouldReturnCorrectValue() {
        userRepository.save(testUser);

        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    @DisplayName("Обновление пользователя")
    void update_ShouldModifyUser() {
        User savedUser = userRepository.save(testUser);

        savedUser.setName("Обновленное имя");
        savedUser.setAge(30);
        User updatedUser = userRepository.save(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Обновленное имя");
        assertThat(updatedUser.getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("Удаление пользователя")
    void delete_ShouldRemoveUser() {
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);

        assertThat(userRepository.findById(userId)).isEmpty();
    }
}