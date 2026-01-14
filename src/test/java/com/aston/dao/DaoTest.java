package com.aston.dao;

import com.aston.entity.User;
import com.aston.exception.UserException;
import com.aston.utils.TestHibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DaoTest {

    private UserDaoImpl userDao;
    private SessionFactory sessionFactory;

    @BeforeAll
    public void beforeAll() {
        TestDatabaseConfig.startContainer();

        String jdbcUrl = TestDatabaseConfig.getJdbcUrl();
        String username = TestDatabaseConfig.getUsername();
        String password = TestDatabaseConfig.getPassword();

        sessionFactory = TestHibernateUtil.getSessionFactory(jdbcUrl, username, password);
        userDao = new UserDaoImpl(sessionFactory);
    }

    @BeforeEach
    public void setUp() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createNativeQuery("DELETE FROM users").executeUpdate();
            session.createNativeQuery("ALTER SEQUENCE IF EXISTS user_id_seq RESTART WITH 1").executeUpdate();
            tx.commit();
        }
        // Проверка, что очищено:
        assertThat(userDao.findAll()).isEmpty();
    }

    @AfterAll
    public void afterAll() {
        sessionFactory.close();
        TestDatabaseConfig.stopContainer();
    }

    @Test
    @DisplayName("Сохранение пользователя - успех")
    public void save_User_ShouldSuccesfully(){
        User user = User.builder()
                .name("Иванов Иван")
                .email("ivan@gmail.com")
                .age(30)
                .build();

        User savedUser = userDao.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Иванов Иван");
        assertThat(savedUser.getEmail()).isEqualTo("ivan@gmail.com");
        assertThat(savedUser.getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("Сохранение пользователя с дублирующимся email - выброс исключения")
    void save_DuplicateEmail_ShouldThrowException() {
        User user1 = User.builder()
                .name("Иван Иванов")
                .email("duplicate@gmail.com")
                .age(30)
                .build();

        User user2 = User.builder()
                .name("Петр Петров")
                .email("duplicate@gmail.com")  // Тот же email
                .age(25)
                .build();

        userDao.save(user1);

        assertThatThrownBy(() -> userDao.save(user2))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    @DisplayName("Поиск пользователя по id - существующий пользователь")
    void findById_ExistingUser_ShouldReturnUser() {
        User user = User.builder()
                .name("Мария Сидорова")
                .email("maria@gmail.com")
                .age(28)
                .build();

        User savedUser = userDao.save(user);

        Optional<User> foundUser = userDao.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Мария Сидорова");
        assertThat(foundUser.get().getEmail()).isEqualTo("maria@gmail.com");
    }

    @Test
    @DisplayName("Поиск пользователя по id - несуществующий пользователь")
    void findById_NonExistingUser_ShouldReturnEmpty() {
        Optional<User> foundUser = userDao.findById(999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Получение всех пользователей - список пользователей")
    void findAll_ShouldReturnAllUsers() {
        User user1 = User.builder()
                .name("Алексей Алексеев")
                .email("alex@gmail.com")
                .age(35)
                .build();

        User user2 = User.builder()
                .name("Ольга Ольгова")
                .email("olga@gmail.com")
                .age(29)
                .build();

        userDao.save(user1);
        userDao.save(user2);

        List<User> users = userDao.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("alex@gmail.com", "olga@gmail.com");
    }

    @Test
    @DisplayName("Поиск пользователей по имени - список пользователей с указаным именем")
    void findByName_ShouldReturnUsersByName() {
        User user1 = User.builder()
                .name("Сергей Сергеев")
                .email("sergey1@gmail.com")
                .age(30)
                .build();

        User user2 = User.builder()
                .name("Сергей Иванов")
                .email("sergey2@gmail.com")
                .age(25)
                .build();

        User user3 = User.builder()
                .name("Анна Сергеева")
                .email("anna@gmail.com")
                .age(28)
                .build();

        userDao.save(user1);
        userDao.save(user2);
        userDao.save(user3);

        List<User> sergeys = userDao.findByName("Сергей");

        assertThat(sergeys).hasSize(2);
        assertThat(sergeys).extracting(User::getName)
                .containsExactlyInAnyOrder("Сергей Сергеев", "Сергей Иванов");
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void findByEmail_ExistingEmail_ShouldReturnUser() {
        User user = User.builder()
                .name("Дмитрий Дмитриев")
                .email("dmitry@gmail.com")
                .age(32)
                .build();

        userDao.save(user);

        Optional<User> foundUser = userDao.findByEmail("dmitry@gmail.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Дмитрий Дмитриев");
    }

    @Test
    @DisplayName("Поиск пользователя по email - несуществующий email")
    void findByEmail_NonExistingEmail_ShouldReturnEmpty() {
        Optional<User> foundUser = userDao.findByEmail("nonexistent@gmail.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Обновление пользователя - успешное обновление")
    void update_ValidUser_ShouldUpdateSuccessfully() {
        User user = User.builder()
                .name("Игорь Игорев")
                .email("igor@gmail.com")
                .age(40)
                .build();

        User savedUser = userDao.save(user);

        savedUser.setName("Игорь Обновленный");
        savedUser.setAge(41);
        User updatedUser = userDao.update(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Игорь Обновленный");
        assertThat(updatedUser.getAge()).isEqualTo(41);
    }

    @Test
    @DisplayName("Удаление пользователя по id - успех удалния")
    void delete_ExistingUser_ShouldDeleteSuccessfully() {
        User user = User.builder()
                .name("Елена Еленова")
                .email("elena@gmail.com")
                .age(27)
                .build();

        User savedUser = userDao.save(user);

        userDao.delete(savedUser.getId());

        Optional<User> foundUser = userDao.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Удаление пользователя по id - исключение несуществующий пользователь")
    void delete_NonExistingUser_ShouldThrowException() {
        assertThatThrownBy(() -> userDao.delete(999L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("Проверка существования пользователя по id")
    void existsById_ShouldReturnCorrectResult() {
        User user = User.builder()
                .name("Василий Васильев")
                .email("vasily@gmail.com")
                .age(45)
                .build();

        User savedUser = userDao.save(user);

        assertThat(userDao.existsById(savedUser.getId())).isTrue();
        assertThat(userDao.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("Проверка существования пользователя по email")
    void existsByEmail_ShouldReturnCorrectResult() {
        User user = User.builder()
                .name("Наталья Натальева")
                .email("natalya@gmail.com")
                .age(33)
                .build();

        userDao.save(user);

        assertThat(userDao.existsByEmail("natalya@gmail.com")).isTrue();
        assertThat(userDao.existsByEmail("nonexistent@gmail.com")).isFalse();
    }

    @Test
    @DisplayName("Сохранение пользователя без возраста")
    void save_UserWithoutAge_ShouldSaveSuccessfully() {
        User user = User.builder()
                .name("Безвозрастный Пользователь")
                .email("noage@example.com")
                .build(); // age = null

        User savedUser = userDao.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getAge()).isNull();
    }
}
