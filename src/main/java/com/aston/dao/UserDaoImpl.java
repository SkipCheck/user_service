package com.aston.dao;

import com.aston.entity.User;
import com.aston.exception.UserException;
import com.aston.utils.HibernateUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@Slf4j
public class UserDaoImpl implements UserDao{

    private final SessionFactory sessionFactory;

    public UserDaoImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public UserDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User save(User user) {
        Transaction transaction = null;
        Session session = getSession();

        try {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
            log.info("Пользователь id-{} сохранен", user.getId());

            return user;
        } catch (Exception e ) {
            if (transaction != null) {
                transaction.rollback();
            }

            String messageException = e instanceof ConstraintViolationException ? "Пользователь с email " + user.getEmail() + " уже существует" : "Не удалось сохранить пользователя";

            log.error("Ошибка при сохранении пользователя: {}", e.getMessage(), e);
            throw new UserException(messageException, e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;

        try (Session session = getSession()){
            transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();

            log.info("Пользователь обновлен с ID: {}", user.getId());
            return user;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка при обновлении пользователя: {}", e.getMessage(), e);
            throw new UserException("Не удалось обновить пользователя", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = getSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        } catch (Exception e) {
            log.error("Ошибка при получении всех пользователей: {}", e.getMessage(), e);
            throw new UserException("Не удалось получить список пользователей", e);
        }
    }

    @Override
    public List<User> findByName(String name) {
        try (Session session = getSession()) {
            Query<User> query = session.createQuery("FROM User WHERE name LIKE :name", User.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        } catch (Exception e) {
            log.error("Ошибка при поиске пользователей по имени {}: {}", name, e.getMessage(), e);
            throw new UserException("Не удалось найти пользователей по имени", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = getSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            User user = query.getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Ошибка при поиске пользователя по email {}: {}", email, e.getMessage(), e);
            throw new UserException("Не удалось найти пользователя по email", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        Session session = getSession();
        try {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("Ошибка при поиске пользователя по ID {}: {}", id, e.getMessage(), e);
            throw new UserException("Не удалось найти пользователя по ID", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();

            User user = session.get(User.class, id);

            session.delete(user);
            log.info("Пользователь удален с ID: {}", id);

            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Ошибка при удалении пользователя с ID {}: {}", id, e.getMessage(), e);
            throw new UserException("Не удалось удалить пользователя", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public boolean existsById(Long id) {
        try (Session session = getSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM User WHERE id = :id", Long.class);
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            log.error("Ошибка при проверке существования пользователя: {}", e.getMessage(), e);
            throw new UserException("Не удалось проверить существование пользователя", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try (Session session = getSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            log.error("Ошибка при проверке существования email: {}", e.getMessage(), e);
            throw new UserException("Не удалось проверить существование email", e);
        }
    }

    private Session getSession() {
        return sessionFactory.openSession();
    }
}
