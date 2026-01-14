package com.aston.unit;

import com.aston.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты для Entity User")
class UserEntityTest {

    @Test
    @DisplayName("Создание пользователя через Builder")
    void userBuilder_ShouldCreateValidUser() {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .id(1L)
                .name("Станислав")
                .email("encoregranted@gmail.com")
                .age(29)
                .createdAt(now)
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Станислав");
        assertThat(user.getEmail()).isEqualTo("encoregranted@gmail.com");
        assertThat(user.getAge()).isEqualTo(29);
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Проверка equals и hashCode")
    void userEqualsAndHashCode_ShouldWorkCorrectly() {
        User user1 = User.builder()
                .id(1L)
                .name("Иван")
                .email("ivan@example.com")
                .age(30)
                .build();

        User user2 = User.builder()
                .id(1L)
                .name("Петр")
                .email("petr@example.com")
                .age(25)
                .build();

        assertThat(user1).isNotEqualTo(user2);
        assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Метод toString() должен содержать информацию о пользователе")
    void userToString_ShouldContainUserInfo() {
        User user = User.builder()
                .id(1L)
                .name("Станислав")
                .email("encoregranted@gmail.com")
                .age(29)
                .build();

        String toString = user.toString();

        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=Станислав");
        assertThat(toString).contains("email=encoregranted@gmail.com");
        assertThat(toString).contains("age=29");
    }

    @Test
    @DisplayName("Создание пользователя с минимальными данными")
    void userWithMinimalData_ShouldCreateSuccessfully() {
        User user = User.builder()
                .name("Мин")
                .email("min@example.com")
                .build();

        assertThat(user.getName()).isEqualTo("Мин");
        assertThat(user.getEmail()).isEqualTo("min@example.com");
        assertThat(user.getAge()).isNull();
        assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("Тест сеттеров и геттеров")
    void testSettersAndGetters() {
        User user = new User();
        LocalDateTime now = LocalDateTime.now();

        user.setId(1L);
        user.setName("Test");
        user.setEmail("test@test.com");
        user.setAge(25);
        user.setCreatedAt(now);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Test");
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getAge()).isEqualTo(25);
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }
}