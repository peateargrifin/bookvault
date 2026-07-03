package com.bookvault.bookvault.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsbnValidator.class)
public @interface ValidIsbn {
    String message() default "Must be a valid ISBN-10 or ISBN-13";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
