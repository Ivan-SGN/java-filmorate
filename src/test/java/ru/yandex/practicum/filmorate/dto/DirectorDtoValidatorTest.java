package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.dto.DirectorDto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectorDtoValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationWhenValid() {
        DirectorDto dto = new DirectorDto()
                .setId(1L)
                .setName("Nolan");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldFailWhenNameIsNull() {
        DirectorDto dto = new DirectorDto()
                .setId(1L)
                .setName(null);

        assertFalse(validator.validate(dto).isEmpty());
    }
}