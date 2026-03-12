package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Mpa {

    private int id;

    @NotBlank
    private String Name;
}
