package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

public class InMemoryMpaStorage implements MpaStorage {
    @Override
    public List<Mpa> getAll() {
        return List.of();
    }

    @Override
    public Optional<Mpa> getById(int id) {
        return Optional.empty();
    }
}
