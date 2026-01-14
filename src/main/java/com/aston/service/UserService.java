package com.aston.service;

import com.aston.dto.UserRequest;
import com.aston.dto.UserResponse;
import com.aston.entity.User;
import com.aston.exception.UserException;
import com.aston.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Сервисный слой приложения
 *
 * Бизнес-логика работы с пользователями
 * Инкапуслирует работу с репозиторием и преобразованием DTO
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Преобразует сущность user в DTO UserResponse
     *
     * @param user сущность пользователя
     * @return DTO для ответа
     */
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .createdAt(user.getCreatedAt())
                .build();
    }
    /**
     * Создание нового пользователя
     *
     * @param userRequest DTO с данными пользователя
     * @return созданный пользователь в виде DTO
     * @throws UserException если пользователь с таким email уже существует
     */
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Создание нового пользователя: name={}, email={}, age={}",
                userRequest.getName(), userRequest.getEmail(), userRequest.getAge());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserException("Пользователь с email '" + userRequest.getEmail() + "' уже существует");
        }

        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .age(userRequest.getAge())
                .build();

        user = userRepository.save(user);

        log.info("Пользователь создан: id={}, email={}", user.getId(), user.getEmail());
        return convertToResponse(user);
    }

    /**
     * Получение пользователя по id
     *
     * @param id id пользователя
     * @return пользователь в виде DTO
     * @throws UserException если пользователь не найден
     */
    public UserResponse getUserById(Long id) {
        log.debug("Получение пользователя по ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Пользователь с ID " + id + " не найден"));

        return convertToResponse(user);
    }

    /**
     * Получение всех пользователей
     *
     * @return список всех пользователей
     */
    public List<UserResponse> getAllUsers() {
        log.debug("Получение всех пользователей");

        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Поиск пользователей по имени
     *
     * @param name имя или часть имени для поиска
     * @return список найденных пользователей
     */
    public List<UserResponse> getUsersByName(String name) {
        log.debug("Поиск пользователей по имени: {}", name);

        return userRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получение пользователя по email
     *
     * @param email email пользователя
     * @return пользователь в виде DTO
     * @throws UserException если пользователь не найден
     */
    public UserResponse getUserByEmail(String email) {
        log.debug("Поиск пользователя по email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("Пользователь с email " + email + " не найден"));

        return convertToResponse(user);
    }

    /**
     * Обновление данных пользователя
     *
     * @param id ID пользователя для обновления
     * @param userRequest новые данные пользователя
     * @return обновленный пользователь в виде DTO
     * @throws UserException если пользователь не найден или email уже занят другим пользователем
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Обновление пользователя с ID {}: name={}, email={}, age={}",
                id, userRequest.getName(), userRequest.getEmail(), userRequest.getAge());

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("Пользователь с ID " + id + " не найден"));

        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            throw new UserException("Пользователь с email '" + userRequest.getEmail() + "' уже существует");
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        user = userRepository.save(user);

        log.info("Пользователь обновлен: ID={}", id);
        return convertToResponse(user);
    }

    /**
     * Удаление пользователя
     *
     * @param id ID пользователя для удаления
     * @throws UserException если пользователь не найден
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserException("Пользователь с ID " + id + " не найден");
        }

        userRepository.deleteById(id);
        log.info("Пользователь удален: ID={}", id);
    }
}