package com.aston.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;


/**
 * Сущность пользователя
 *
 * Представление таблицы пользователй в БД
 * Использует JPA анотации для маппинга на таблицу
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {


    /**
     * Уникальный идентификатор пользователя
     * генерируемый на стороне БД
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * Имя пользователя
     */
    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Email пользователя
     */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;


    /**
     * Возраст пользователя
     */
    @Min(value = 0, message = "Возраст не может быть отрицательным")
    @Max(value = 150, message = "Возраст не может превышать 150 лет")
    @Column(name = "age")
    private Integer age;

    /**
     * Дата создания пользователя
     * Атвоматически создается при создании записи
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}