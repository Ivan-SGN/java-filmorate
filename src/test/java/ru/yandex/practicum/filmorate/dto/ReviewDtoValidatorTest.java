package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.dto.ReviewDto;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewDtoValidatorTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validReview_hasNoViolations() {
        assertThat(validator.validate(validReviewDto())).isEmpty();
    }

    @Test
    void blankContent() {
        var dto = validReviewDto();
        dto.setContent(" ");

        var violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    void nullPositive() {
        var dto = validReviewDto();
        dto.setPositive(null);

        var violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("positive");
    }

    @Test
    void nullUserId() {
        var dto = validReviewDto();
        dto.setUserId(null);

        var violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("userId");
    }

    @Test
    void nullFilmId() {
        var dto = validReviewDto();
        dto.setFilmId(null);

        var violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("filmId");
    }

    @Test
    void reviewIdAndUseful_haveNoBeanValidationConstraints() {
        var dto = validReviewDto();
        dto.setReviewId(-1L);
        dto.setUseful(-10);

        assertThat(validator.validate(dto)).isEmpty();
    }

    private static ReviewDto validReviewDto() {
        var dto = new ReviewDto();
        dto.setReviewId(1L);
        dto.setContent("This film is great.");
        dto.setPositive(true);
        dto.setUserId(1);
        dto.setFilmId(1);
        dto.setUseful(0);
        return dto;
    }
}
