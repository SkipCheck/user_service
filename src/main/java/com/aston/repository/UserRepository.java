package com.aston.repository;

import com.aston.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью user в БД
 *
 * Наследует класс JpaRepository, благодаря чему использует операции CRUD
 * Spring Data автоматически формирует методы по их названиям
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Найти пользователя по email
     *
     * @param email email пользователя
     * @return Optional с пользователем или пустой Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * Найти пользователей по имени
     *
     * @param name часть имени для поиска
     * @return список пользователей
     */
    List<User> findByNameContainingIgnoreCase(String name);

    /**
     * Проверить существование пользователя с указанным email
     *
     * @param email email для проверки
     * @return true если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Проверить существование пользователя с email, но исключая пользователя с указанным id
     * Используется при обновлении пользователя, чтобы разрешить сохранение того же email
     *
     * @param email email для проверки
     * @param id ID пользователя для исключения
     * @return true если существует другой пользователь с таким email
     */
    @Query("SELECT CASE WHEN COUNT(user) > 0 THEN true ELSE false END FROM User user WHERE user.email = :email AND user.id <> :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}