package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MpaTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testNameMustNotBeBlank() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("");

        Set<ConstraintViolation<Mpa>> violations = validator.validate(mpa);

        assertEquals(1, violations.size());
        ConstraintViolation<Mpa> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testValidMpaPassesValidation() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("PG-13");

        Set<ConstraintViolation<Mpa>> violations = validator.validate(mpa);

        assertTrue(violations.isEmpty());
    }
}