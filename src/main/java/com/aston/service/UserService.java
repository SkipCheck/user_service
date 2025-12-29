package com.aston.service;

import com.aston.dao.UserDao;
import com.aston.dao.UserDaoImpl;
import com.aston.entity.User;
import com.aston.exception.UserException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UserService {

    private UserDao userDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
    }

    public User createUser(String name, String email, Integer age) {
        log.info("Создание нового пользователя: name={}, email={}, age={}", name, email, age);

        validateUserData(name, email, age);

        if (userDao.existsByEmail(email)) {
            throw new UserException("Пользователь с email '" + email + "' уже существует");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .age(age)
                .createdAt(LocalDateTime.now())
                .build();

        return userDao.save(user);
    }

    public Optional<User> getUserById(Long id) {
        log.info("Получение пользователя по ID: {}", id);
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        log.info("Получение всех пользователей");
        return userDao.findAll();
    }

    public List<User> getUsersByName(String name) {
        log.info("Поиск пользователей по имени: {}", name);
        return userDao.findByName(name);
    }

    public Optional<User> getUserByEmail(String email) {
        log.info("Поиск пользователя по email: {}", email);
        return userDao.findByEmail(email);
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        log.info("Обновление пользователя с ID {}: name={}, email={}, age={}",
                id, name, email, age);

        validateUserData(name, email, age);

        Optional<User> existingUser = userDao.findById(id);
        if (existingUser.isEmpty()) {
            throw new UserException("Пользователь с ID " + id + " не найден");
        }

        User user = User.builder()
                .id(id)
                .name(name)
                .email(email)
                .age(age)
                .build();

        return userDao.update(user);
    }

    public void deleteUser(Long id) {
        log.info("Удаление пользователя с ID: {}", id);
        userDao.delete(id);
    }

    private void validateUserData(String name, String email, Integer age) {
        if (name == null || name.trim().isEmpty()) {
            throw new UserException("Имя пользователя не может быть пустым");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new UserException("Email не может быть пустым");
        }

        String trimmedEmail = email.trim();

        // Более строгая, но реалистичная валидация email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        //валидация почты через регулярки
        if (!trimmedEmail.matches(emailRegex)) {
            throw new UserException("Некорректный формат email");
        }

        if (age != null && (age < 0 || age > 150)) {
            throw new UserException("Возраст должен быть от 0 до 150 лет");
        }
    }
}
