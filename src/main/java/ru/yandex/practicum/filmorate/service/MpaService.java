package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
public class MpaService {

    private final MpaStorage mpaStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<Mpa> getAllMpa() {
        log.info("Get all mpa request");
        return mpaStorage.getAll();
    }

    public Mpa getMpa(int id) {
        log.info("Get mpa request, id: {}", id);
        return getMpaOrThrow(id);
    }

    private Mpa getMpaOrThrow(int id) {
        return mpaStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("MPA not found, id={}", id);
                    return new NotFoundException("MPA not found");
                });
    }
}