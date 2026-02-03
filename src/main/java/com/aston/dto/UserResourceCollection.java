package com.aston.dto;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

/**
 * Обертка для коллекции пользователей с поддержкой HATEOAS
 */
public class UserResourceCollection extends CollectionModel<EntityModel<UserResource>> {

    public UserResourceCollection(Iterable<EntityModel<UserResource>> content) {
        super(content);
    }
}