package ru.yandex.practicum.filmorate.controller.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GenreDto {
    private Long id;
    private String name;
}
