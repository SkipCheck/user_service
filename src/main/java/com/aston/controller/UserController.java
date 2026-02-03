package com.aston.controller;

import com.aston.dto.UserRequest;
import com.aston.dto.UserResource;
import com.aston.dto.UserResourceCollection;
import com.aston.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST-контроллер для работы с пользователем
 *
 * Обработка HTTP-запросов
 * Включает в себя эндпоинты для работы CRUD-операций
 * с поддержкой Swagger документации и HATEOAS
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Создать нового пользователя",
            description = "Создает нового пользователя с указанными данными"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные пользователя",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EntityModel<UserResource>> createUser(
            @Parameter(description = "Данные нового пользователя", required = true)
            @Valid @RequestBody UserRequest userRequest) {

        log.info("POST /api/v1/users - Создание пользователя: {}", userRequest.getEmail());
        UserResource response = userService.createUser(userRequest);

        EntityModel<UserResource> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(UserController.class).getUserById(response.getId())).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).updateUser(response.getId(), userRequest)).withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(response.getId())).withRel("delete"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return ResponseEntity
                .created(resource.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(resource);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает информацию о пользователе по его идентификатору"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EntityModel<UserResource>> getUserById(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id) {

        log.debug("GET /api/v1/users/{} - Получение пользователя по ID", id);
        UserResource response = userService.getUserById(id);

        EntityModel<UserResource> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).updateUser(id, new UserRequest())).withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));
        resource.add(linkTo(methodOn(UserController.class).getUserByEmail(response.getEmail())).withRel("by-email"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping
    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей в системе"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = UserResourceCollection.class))
            )
    })
    public ResponseEntity<UserResourceCollection> getAllUsers() {

        log.debug("GET /api/v1/users - Получение всех пользователей");
        List<UserResource> users = userService.getAllUsers();

        List<EntityModel<UserResource>> userResources = users.stream()
                .map(user -> {
                    EntityModel<UserResource> resource = EntityModel.of(user);
                    resource.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
                    resource.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), new UserRequest())).withRel("update"));
                    resource.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
                    return resource;
                })
                .collect(Collectors.toList());

        UserResourceCollection collection = new UserResourceCollection(userResources);
        collection.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
        collection.add(linkTo(methodOn(UserController.class).createUser(new UserRequest())).withRel("create"));

        return ResponseEntity.ok(collection);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Поиск пользователей по имени",
            description = "Ищет пользователей по части имени (без учета регистра)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Поиск выполнен успешно",
                    content = @Content(schema = @Schema(implementation = UserResourceCollection.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Не указан параметр name",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<UserResourceCollection> getUsersByName(
            @Parameter(description = "Имя или часть имени для поиска", required = true, example = "John")
            @RequestParam String name) {

        log.debug("GET /api/v1/users/search?name={} - Поиск пользователей по имени", name);
        List<UserResource> users = userService.getUsersByName(name);

        List<EntityModel<UserResource>> userResources = users.stream()
                .map(user -> {
                    EntityModel<UserResource> resource = EntityModel.of(user);
                    resource.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
                    return resource;
                })
                .collect(Collectors.toList());

        UserResourceCollection collection = new UserResourceCollection(userResources);
        collection.add(linkTo(methodOn(UserController.class).getUsersByName(name)).withSelfRel());
        collection.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(collection);
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Получить пользователя по email",
            description = "Возвращает информацию о пользователе по его email"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EntityModel<UserResource>> getUserByEmail(
            @Parameter(description = "Email пользователя", required = true, example = "user@example.com")
            @PathVariable String email) {

        log.debug("GET /api/v1/users/email/{} - Поиск пользователя по email", email);
        UserResource response = userService.getUserByEmail(email);

        EntityModel<UserResource> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(UserController.class).getUserByEmail(email)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getUserById(response.getId())).withRel("by-id"));
        resource.add(linkTo(methodOn(UserController.class).updateUser(response.getId(), new UserRequest())).withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(response.getId())).withRel("delete"));

        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить данные пользователя",
            description = "Обновляет информацию о пользователе по его ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные пользователя",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<EntityModel<UserResource>> updateUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Новые данные пользователя", required = true)
            @Valid @RequestBody UserRequest userRequest) {

        log.info("PUT /api/v1/users/{} - Обновление пользователя", id);
        UserResource response = userService.updateUser(id, userRequest);

        EntityModel<UserResource> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).updateUser(id, userRequest)).withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users"));

        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по его ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь успешно удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Long id) {

        log.info("DELETE /api/v1/users/{} - Удаление пользователя", id);
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}