package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Review {
    private int reviewId;

    private String content;

    private boolean positive;

    private int userId;

    private int filmId;

    private int useful;
}
