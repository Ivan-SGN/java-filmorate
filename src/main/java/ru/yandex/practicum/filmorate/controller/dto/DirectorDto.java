package ru.yandex.practicum.filmorate.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DirectorDto {
    private Long id;
    @NotBlank(message = "Name must not be empty")
    private String name;
}