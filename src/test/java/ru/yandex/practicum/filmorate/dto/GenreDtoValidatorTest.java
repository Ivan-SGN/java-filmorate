package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.controller.dto.GenreDto;

import static org.assertj.core.api.Assertions.assertThat;

class GenreDtoValidatorTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validGenre_hasNoViolations() {
        var dto = new GenreDto()
                .setId(1L)
                .setName("Comedy");

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 1L, Long.MAX_VALUE})
    void id_hasNoValidationConstraints(long id) {
        var dto = new GenreDto()
                .setId(id)
                .setName("Genre");

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void name_hasNoValidationConstraints() {
        var dto = new GenreDto()
                .setId(1L)
                .setName("");

        assertThat(validator.validate(dto)).isEmpty();
    }
}
