package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testNameMustNotBeBlank() {
        Film film = new Film();
        film.setName("");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("", violation.getInvalidValue());
    }

    @Test
    void testDescriptionMustNotExceedMaxLength() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    void testReleaseDateMustNotBeNull() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("description");
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("releaseDate", violation.getPropertyPath().toString());
    }

    @Test
    void testDurationMustBePositive() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals("duration", violation.getPropertyPath().toString());
    }

    @Test
    void testValidFilmPassesValidation() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }
}