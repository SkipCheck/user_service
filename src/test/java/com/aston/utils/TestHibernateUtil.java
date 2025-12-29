package com.aston.utils;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

@Slf4j
public class TestHibernateUtil {
    private static SessionFactory sessionFactory;

    public static synchronized SessionFactory getSessionFactory(String jdbcUrl, String username, String password) {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            initializeSessionFactory(jdbcUrl, username, password);
        }
        return sessionFactory;
    }

    private static void initializeSessionFactory(String jdbcUrl, String username, String password) {
        try {
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .configure("hibernate-test.cfg.xml")
                    .applySetting("hibernate.connection.url", jdbcUrl)
                    .applySetting("hibernate.connection.username", username)
                    .applySetting("hibernate.connection.password", password)
                    .build();

            Metadata metadata = new MetadataSources(standardRegistry)
                    .getMetadataBuilder()
                    .build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();

            log.info("Test Hibernate SessionFactory создана успешно для URL: {}", jdbcUrl);

        } catch (Exception e) {
            log.error("Ошибка при создании тестовой SessionFactory: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать тестовую SessionFactory", e);
        }
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            log.info("Test SessionFactory закрыта");
        }
    }
}