package com.aston.service;

import com.aston.dao.UserDao;
import com.aston.entity.User;
import com.aston.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Юнит-тесты для UserService")
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Станислав")
                .email("encoregranted@gmail.com")
                .age(29)
                .createdAt(LocalDateTime.now())
                .build();

        // убедимся, что userDao установлен (для отладки)
        assertThat(userDao).isNotNull();
        assertThat(userService).isNotNull();
    }

    @Test
    @DisplayName("Проверка настройки моков")
    void verifyMockSetup() {
        // этот тест проверяет, что моки правильно настроены
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Станислав");

        verify(userDao).findById(1L);
        System.out.println("✓ Моки настроены правильно!");
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными")
    void createUser_ValidData_ShouldCreateSuccessfully() {
        when(userDao.existsByEmail("encoregranted@gmail.com")).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(testUser);
        
        User createdUser = userService.createUser("Станислав", "encoregranted@gmail.com", 29);
        
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("Станислав");
        assertThat(createdUser.getEmail()).isEqualTo("encoregranted@gmail.com");
        assertThat(createdUser.getAge()).isEqualTo(29);

        verify(userDao).existsByEmail("encoregranted@gmail.com");
        verify(userDao).save(any(User.class));

        // проверяем, что не было других вызовов
        verifyNoMoreInteractions(userDao);
    }

    @Test
    @DisplayName("Создание пользователя с существующим email")
    void createUser_DuplicateEmail_ShouldThrowException() {
        when(userDao.existsByEmail("encoregranted@gmail.com")).thenReturn(true);

        assertThatThrownBy(() ->
                userService.createUser("Станислав", "encoregranted@gmail.com", 29))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("уже существует");

        verify(userDao).existsByEmail("encoregranted@gmail.com");
        verify(userDao, never()).save(any(User.class));
    }

    @ParameterizedTest
    @CsvSource({
            "'', 'test@example.com', 25, 'Имя пользователя не может быть пустым'",
            "'Иван', '', 25, 'Email не может быть пустым'",
            "'Иван', 'invalid-email', 25, 'Некорректный формат email'",
            "'Иван', 'test@example.com', -1, 'Возраст должен быть от 0 до 150 лет'",
            "'Иван', 'test@example.com', 151, 'Возраст должен быть от 0 до 150 лет'"
    })
    @DisplayName("Создание пользователя с некорректными данными")
    void createUser_InvalidData_ShouldThrowException(
            String name, String email, Integer age, String expectedMessage) {
        assertThatThrownBy(() -> userService.createUser(name, email, age))
                .isInstanceOf(UserException.class)
                .hasMessage(expectedMessage);

        // Проверяем, что DAO методы не вызывались
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("Получение пользователя по существующему ID")
    void getUserById_ExistingId_ShouldReturnUser() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Станислав");
        verify(userDao).findById(1L);
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ID")
    void getUserById_NonExistingId_ShouldReturnEmpty() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(999L);

        assertThat(result).isEmpty();
        verify(userDao).findById(999L);
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers_ShouldReturnAllUsers() {
        
        List<User> users = Arrays.asList(
                testUser,
                User.builder()
                        .id(2L)
                        .name("Олег")
                        .email("oleg@gmail.com")
                        .age(23)
                        .build()
        );

        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getName)
                .containsExactly("Станислав", "Олег");
        verify(userDao).findAll();
    }

    @Test
    @DisplayName("Поиск пользователей по имени")
    void getUsersByName_ShouldReturnFilteredUsers() {
        List<User> users = Arrays.asList(testUser);
        when(userDao.findByName("Станислав")).thenReturn(users);
        
        List<User> result = userService.getUsersByName("Станислав");
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Станислав");
        verify(userDao).findByName("Станислав");
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void getUserByEmail_ExistingEmail_ShouldReturnUser() {
        when(userDao.findByEmail("encoregranted@gmail.com")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByEmail("encoregranted@gmail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("encoregranted@gmail.com");
        verify(userDao).findByEmail("encoregranted@gmail.com");
    }

    @Test
    @DisplayName("Обновление пользователя с тем же email")
    void updateUser_SameEmail_ShouldUpdateSuccessfully() {
        when(userDao.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDao.update(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, "Станислав", "encoregranted@gmail.com", 29);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Станислав");
        assertThat(result.getEmail()).isEqualTo("encoregranted@gmail.com");

        verify(userDao).findById(1L);
        verify(userDao, never()).existsByEmail(anyString());
        verify(userDao).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    void updateUser_NonExistingUser_ShouldThrowException() {
        when(userDao.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.updateUser(999L, "Имя", "email@example.com", 30))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("не найден");

        verify(userDao).findById(999L);
        verify(userDao, never()).existsByEmail(anyString());
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Удаление пользователя - успешно")
    void deleteUser_ShouldCallDaoDelete() {
        
        doNothing().when(userDao).delete(1L);

        
        userService.deleteUser(1L);

        
        verify(userDao).delete(1L);
    }

    @Test
    @DisplayName("Удаление пользователя - ошибка в DAO")
    void deleteUser_WhenDaoThrowsException_ShouldPropagate() {
        
        doThrow(new UserException("Ошибка удаления из DAO"))
                .when(userDao).delete(1L);

        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Ошибка удаления из DAO");

        verify(userDao).delete(1L);
    }

    @Test
    @DisplayName("Удаление пользователя с несуществующим ID - ошибка")
    void deleteUser_NonExistingId_ShouldPropagateException() {
        
        doThrow(new UserException("Пользователь не найден"))
                .when(userDao).delete(999L);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(userDao).delete(999L);
    }

    @Test
    @DisplayName("Создание пользователя без возраста")
    void createUser_WithoutAge_ShouldCreateSuccessfully() {
        
        User userWithoutAge = User.builder()
                .id(2L)
                .name("Без Возраста")
                .email("noage@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        when(userDao.existsByEmail("noage@example.com")).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(userWithoutAge);

        
        User result = userService.createUser("Без Возраста", "noage@example.com", null);

        
        assertThat(result).isNotNull();
        assertThat(result.getAge()).isNull();

        verify(userDao).existsByEmail("noage@example.com");
        verify(userDao).save(any(User.class));
    }

    @Test
    @DisplayName("Поиск по email - не найден")
    void getUserByEmail_NonExistingEmail_ShouldReturnEmpty() {
        
        when(userDao.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        
        Optional<User> result = userService.getUserByEmail("nonexistent@example.com");

        
        assertThat(result).isEmpty();
        verify(userDao).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Валидация email с правильным форматом")
    void createUser_ValidEmailFormat_ShouldCreateSuccessfully() {
        
        when(userDao.existsByEmail("valid.email+tag@example.co.uk")).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(testUser);

        
        User result = userService.createUser("Тест", "valid.email+tag@example.co.uk", 25);

        
        assertThat(result).isNotNull();
        verify(userDao).existsByEmail("valid.email+tag@example.co.uk");
        verify(userDao).save(any(User.class));
    }
}