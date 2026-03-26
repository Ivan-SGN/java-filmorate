package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenreTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testNameMustNotBeBlank() {
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("");

        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        assertEquals(1, violations.size());
        ConstraintViolation<Genre> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void testValidGenrePassesValidation() {
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");

        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);

        assertTrue(violations.isEmpty());
    }
}