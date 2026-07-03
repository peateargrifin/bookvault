package com.bookvault.bookvault.service;

import com.bookvault.bookvault.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

// 📘 CONCEPT: Video 14 - Producer side of task queue
// Creates tasks and pushes them to RabbitMQ queue
// Does NOT execute the actual logic — that's the worker's job
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailJobService {

    private final RabbitTemplate rabbitTemplate;

    // 📘 CONCEPT: Video 14 - One-off task (triggered by user registration)
    // 🟡 NOVICE: send email here synchronously → blocks registration response
    // 🏢 PRODUCT: push to queue → registration returns 201 instantly
    //             worker sends email in background within seconds
    public void sendWelcomeEmail(UUID userId, String email, String name) {
        Map<String, String> payload = Map.of(
                "userId", userId.toString(),
                "email",  email,
                "name",   name,
                "type",   "WELCOME"
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKVAULT_EXCHANGE,
                RabbitMQConfig.WELCOME_EMAIL_KEY,
                payload
        );

        // 📘 CONCEPT: Video 18 - Log the job dispatch not the execution
        // Execution happens in worker (different log context)
        log.info("EMAIL_JOB_DISPATCHED type=WELCOME userId={}", userId);
    }

    public void sendBookPublishedNotification(UUID bookId,
            UUID authorId, String bookTitle) {
        Map<String, String> payload = Map.of(
                "bookId",    bookId.toString(),
                "authorId",  authorId.toString(),
                "bookTitle", bookTitle,
                "type",      "BOOK_PUBLISHED"
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOOKVAULT_EXCHANGE,
                RabbitMQConfig.BOOK_PUBLISHED_KEY,
                payload
        );

        log.info("EMAIL_JOB_DISPATCHED type=BOOK_PUBLISHED bookId={}", bookId);
    }
}
