package com.bookvault.bookvault.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 📘 CONCEPT: Video 14 - Task Queue infrastructure
// 🟡 NOVICE: send email synchronously in register API
//             → API hangs if email provider is slow/down
//             → user waits 5+ seconds for registration to complete
// 🏢 PRODUCT: push to queue → return 201 instantly
//             worker picks up task in background → retries if provider fails
@Configuration
public class RabbitMQConfig {

    // Queue names
    public static final String WELCOME_EMAIL_QUEUE     = "bookvault.email.welcome";
    public static final String BOOK_PUBLISHED_QUEUE    = "bookvault.book.published";
    public static final String DEAD_LETTER_QUEUE       = "bookvault.dead-letter";

    // Exchange names
    public static final String BOOKVAULT_EXCHANGE      = "bookvault.exchange";
    public static final String DEAD_LETTER_EXCHANGE    = "bookvault.dlx";

    // Routing keys
    public static final String WELCOME_EMAIL_KEY       = "email.welcome";
    public static final String BOOK_PUBLISHED_KEY      = "book.published";

    // 📘 CONCEPT: Video 14 - Dead Letter Queue
    // When a task fails MAX retries → goes to DLQ instead of being lost
    // 🏢 PRODUCT: ops team monitors DLQ → manually retry or investigate
    //             No task ever silently disappears
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_QUEUE);
    }

    // Main exchange
    @Bean
    public DirectExchange bookvaultExchange() {
        return new DirectExchange(BOOKVAULT_EXCHANGE);
    }

    // Welcome email queue
    @Bean
    public Queue welcomeEmailQueue() {
        return QueueBuilder.durable(WELCOME_EMAIL_QUEUE)
                // 📘 CONCEPT: Video 14 - Dead letter config per queue
                // Failed messages → dead letter exchange after max retries
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding welcomeEmailBinding() {
        return BindingBuilder
                .bind(welcomeEmailQueue())
                .to(bookvaultExchange())
                .with(WELCOME_EMAIL_KEY);
    }

    // Book published queue
    @Bean
    public Queue bookPublishedQueue() {
        return QueueBuilder.durable(BOOK_PUBLISHED_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding bookPublishedBinding() {
        return BindingBuilder
                .bind(bookPublishedQueue())
                .to(bookvaultExchange())
                .with(BOOK_PUBLISHED_KEY);
    }

    // 📘 CONCEPT: Video 7 - Serialization for message queues
    // Messages are serialized to JSON when pushed to queue
    // Deserialized back to Java objects when consumer picks up
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
