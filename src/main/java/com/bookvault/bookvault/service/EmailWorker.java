package com.bookvault.bookvault.service;

import com.bookvault.bookvault.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

// 📘 CONCEPT: Video 14 - Consumer side of task queue
// Runs in a SEPARATE thread from the main HTTP server thread
// Picks up tasks from queue and executes them
// 🟡 NOVICE: everything in main thread → email failure = API failure
// 🏢 PRODUCT: worker is isolated → email failure = retry in background
//             main API thread never knows or cares about email status
@Component
@Slf4j
public class EmailWorker {

    // 📘 CONCEPT: Video 14 - @RabbitListener registers this as a consumer
    // Spring AMQP continuously polls the queue for new messages
    // When message arrives → calls this method automatically
    @RabbitListener(queues = RabbitMQConfig.WELCOME_EMAIL_QUEUE)
    public void handleWelcomeEmail(Map<String, String> payload) {
        String userId = payload.get("userId");
        String email  = payload.get("email");
        String name   = payload.get("name");

        try {
            log.info("EMAIL_WORKER_START type=WELCOME userId={} email={}",
                    userId, email);

            // 📘 CONCEPT: Video 14 - Simulate email sending
            // In real app: inject EmailProvider and call their API
            // e.g., resendClient.send(to: email, subject: "Welcome!", ...)
            // For now: simulate with a log
            simulateSendEmail(email, "Welcome to BookVault, " + name + "!",
                    "Your account has been created successfully.");

            log.info("EMAIL_WORKER_SUCCESS type=WELCOME userId={}", userId);

        } catch (Exception e) {
            // 📘 CONCEPT: Video 14 - Retry mechanism
            // Throwing exception here → RabbitMQ requeues the message
            // After max retries → message goes to Dead Letter Queue
            log.error("EMAIL_WORKER_FAILED type=WELCOME userId={} error={}",
                    userId, e.getMessage());
            throw e; // rethrow → triggers RabbitMQ retry
        }
    }

    @RabbitListener(queues = RabbitMQConfig.BOOK_PUBLISHED_QUEUE)
    public void handleBookPublished(Map<String, String> payload) {
        String bookId    = payload.get("bookId");
        String authorId  = payload.get("authorId");
        String bookTitle = payload.get("bookTitle");

        try {
            log.info("EMAIL_WORKER_START type=BOOK_PUBLISHED bookId={}", bookId);

            simulateSendEmail(authorId,
                    "Your book is live: " + bookTitle,
                    "Congratulations! Your book is now published on BookVault.");

            log.info("EMAIL_WORKER_SUCCESS type=BOOK_PUBLISHED bookId={}", bookId);

        } catch (Exception e) {
            log.error("EMAIL_WORKER_FAILED type=BOOK_PUBLISHED bookId={} error={}",
                    bookId, e.getMessage());
            throw e;
        }
    }

    // 📘 CONCEPT: Video 14 - Scheduled/Recurring job
    // Runs every day at midnight → cleans up old DRAFT books
    // 🏢 PRODUCT: prevents DB bloat, removes abandoned books
    @org.springframework.scheduling.annotation.Scheduled(
            cron = "0 0 0 * * *") // midnight every day
    public void cleanupOldDraftBooks() {
        log.info("SCHEDULED_JOB_START type=CLEANUP_DRAFTS");
        // In real app: bookRepository.deleteOldDrafts(30 days)
        log.info("SCHEDULED_JOB_DONE type=CLEANUP_DRAFTS");
    }

    private void simulateSendEmail(String to, String subject, String body) {
        // Simulates network call to email provider
        log.info("EMAIL_SENT to={} subject={}", to, subject);
    }
}
