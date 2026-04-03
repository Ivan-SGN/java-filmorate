package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class Director {
    @NonNull
    private int id;

    private String name;

    public Director(int id, String name) {
        this.setId(id);
        this.setName(name);
    }

}