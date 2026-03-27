package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.controller.dto.MpaDto;

import static org.assertj.core.api.Assertions.assertThat;

class MpaDtoValidatorTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validMpa_hasNoViolations() {
        var dto = new MpaDto()
                .setId(1L)
                .setName("PG-13");

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 1L, Long.MAX_VALUE})
    void id_hasNoValidationConstraints(long id) {
        var dto = new MpaDto()
                .setId(id)
                .setName("Rating");

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void name_hasNoValidationConstraints() {
        var dto = new MpaDto()
                .setId(1L)
                .setName("");

        assertThat(validator.validate(dto)).isEmpty();
    }
}
