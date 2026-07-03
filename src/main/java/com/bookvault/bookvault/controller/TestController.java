package com.bookvault.bookvault.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 📘 CONCEPT: Video 19 - Endpoint to manually test graceful shutdown
// In production you'd NEVER have this — it's purely for learning/testing
// Simulates a slow operation (like processing a payment) that takes 10 seconds
@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "Testing", description = "Endpoints for testing infrastructure behavior")
@Slf4j
public class TestController {

    @GetMapping("/slow-request")
    @Operation(summary = "Simulates a 10-second operation (for testing graceful shutdown)")
    public String slowRequest() throws InterruptedException {
        log.info("SLOW_REQUEST_START - simulating 10 second operation");

        for (int i = 1; i <= 10; i++) {
            Thread.sleep(1000);
            log.info("SLOW_REQUEST_PROGRESS second={}", i);
        }

        log.info("SLOW_REQUEST_COMPLETE");
        return "Slow request completed successfully after 10 seconds";
    }
}
