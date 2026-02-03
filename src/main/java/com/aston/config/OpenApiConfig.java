package com.aston.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springdoc.core.Constants.*;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "User Service API",
                version = "1.0",
                description = "API для управления пользователями с поддержкой событий Kafka",
                contact = @Contact(
                        name = "Поддержка",
                        email = "support@aston.example.com",
                        url = "https://aston.example.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        servers = {
                @Server(
                        description = "Локальный сервер",
                        url = "http://localhost:5665/api"
                ),
                @Server(
                        description = "Продуктивный сервер",
                        url = "https://api.aston.example.com"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public OperationCustomizer customizeOperation() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // Добавляем стандартные HTTP коды ответов для всех операций
            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }

            ApiResponses responses = operation.getResponses();

            // Добавляем ответ 400 Bad Request для всех операций
            if (!responses.containsKey("400")) {
                responses.addApiResponse("400", new ApiResponse()
                        .description("Некорректный запрос")
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>()
                                                .$ref("#/components/schemas/ErrorResponse")))));
            }

            // Добавляем ответ 401 Unauthorized для операций требующих аутентификации
            if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                if (!responses.containsKey("401")) {
                    responses.addApiResponse("401", new ApiResponse()
                            .description("Не авторизован")
                            .content(new Content()
                                    .addMediaType("application/json",
                                            new MediaType().schema(new Schema<>()
                                                    .$ref("#/components/schemas/ErrorResponse")))));
                }
            }

            // Добавляем ответ 403 Forbidden
            if (!responses.containsKey("403")) {
                responses.addApiResponse("403", new ApiResponse()
                        .description("Доступ запрещен")
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>()
                                                .$ref("#/components/schemas/ErrorResponse")))));
            }

            // Добавляем ответ 500 Internal Server Error для всех операций
            if (!responses.containsKey("500")) {
                responses.addApiResponse("500", new ApiResponse()
                        .description("Внутренняя ошибка сервера")
                        .content(new Content()
                                .addMediaType("application/json",
                                        new MediaType().schema(new Schema<>()
                                                .$ref("#/components/schemas/ErrorResponse")))));
            }

            return operation;
        };
    }
}