package com.bookvault.bookvault.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// 📘 CONCEPT: Video 9 - Custom validator for complex syntactic rules
// 🟡 NOVICE: @Pattern with complex regex → hard to read and maintain
// 🏢 PRODUCT: dedicated validator class → clear logic, easy to test
public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null || isbn.isBlank()) {
            return true; // ISBN is optional — null is valid
        }
        String cleaned = isbn.replaceAll("[\\s-]", "");
        return isValidIsbn10(cleaned) || isValidIsbn13(cleaned);
    }

    private boolean isValidIsbn10(String isbn) {
        if (isbn.length() != 10) return false;
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            if (!Character.isDigit(isbn.charAt(i))) return false;
            sum += (isbn.charAt(i) - '0') * (10 - i);
        }
        char last = isbn.charAt(9);
        sum += (last == 'X') ? 10 : (last - '0');
        return sum % 11 == 0;
    }

    private boolean isValidIsbn13(String isbn) {
        if (isbn.length() != 13) return false;
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            if (!Character.isDigit(isbn.charAt(i))) return false;
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }
}
