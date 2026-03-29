package ru.yandex.practicum.filmorate.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReviewDto {
    private Long reviewId;

    @NotBlank
    private String content;

    @NotNull
    @JsonProperty("isPositive")
    private Boolean positive;

    @Positive
    @NotNull
    private Integer userId;

    @Positive
    @NotNull
    private Integer filmId;

    private Integer useful;
}
