package com.aston.service;

import com.aston.dto.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.user-events}")
    private String userEventsTopic;

    public void sendUserCreatedEvent(Long userId, String email, String name) {
        UserEvent event = UserEvent.builder()
                .eventType("USER_CREATED")
                .userId(userId)
                .email(email)
                .name(name)
                .build();

        sendEvent(event);
    }

    public void sendUserDeletedEvent(Long userId, String email, String name) {
        UserEvent event = UserEvent.builder()
                .eventType("USER_DELETED")
                .userId(userId)
                .email(email)
                .name(name)
                .build();

        sendEvent(event);
    }

    private void sendEvent(UserEvent event) {
        try {
            kafkaTemplate.send(userEventsTopic, event);
            log.info("Событие отправлено в Kafka: {}", event);
        } catch (Exception e) {
            log.error("Ошибка при отправке события в Kafka: {}", e.getMessage(), e);
        }
    }

}
