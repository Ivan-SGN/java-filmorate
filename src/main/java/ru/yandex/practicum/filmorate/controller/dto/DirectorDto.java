package ru.yandex.practicum.filmorate.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@NotBlank
@Accessors(chain = true)
public class DirectorDto {
    private Long id;
    private String name;
}