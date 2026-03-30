package ru.yandex.practicum.filmorate.controller.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsAfterDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface After {
    String message() default "Date must be after {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value();

    boolean inclusive() default true;
}
