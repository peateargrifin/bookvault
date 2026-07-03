package com.bookvault.bookvault.exception;

// 📘 CONCEPT: Video 16 - Custom exceptions with semantic meaning
// 🟡 NOVICE: throw new RuntimeException("Book not found")
// 🏢 PRODUCT: typed exception → GlobalExceptionHandler maps to exact HTTP status
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException book(Object id) {
        // 📘 CONCEPT: Video 20 - Same message for "not found" and "no access"
        // Attacker can't distinguish between nonexistent vs unauthorized resource
        return new ResourceNotFoundException("Book not found");
    }

    public static ResourceNotFoundException author(Object id) {
        return new ResourceNotFoundException("Author not found");
    }
}
