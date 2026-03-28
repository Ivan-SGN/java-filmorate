package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.dto.MpaDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Slf4j
@Service
public class MpaService {

    private final MpaStorage mpaStorage;
    private final MpaMapper mpaMapper;

    public MpaService(@Qualifier("mpaDbStorage") MpaStorage mpaStorage, MpaMapper mpaMapper) {
        this.mpaStorage = mpaStorage;
        this.mpaMapper = mpaMapper;
    }

    public Collection<MpaDto> getAllMpa() {
        log.info("Get all mpa request");
        return mpaStorage.getAll().stream()
                .map(mpaMapper::mapToDto)
                .toList();
    }

    public MpaDto getMpa(int id) {
        log.info("Get mpa request, id: {}", id);
        return mpaMapper.mapToDto(getMpaOrThrow(id));
    }

    private Mpa getMpaOrThrow(int id) {
        return mpaStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("MPA not found, id={}", id);
                    return new NotFoundException("MPA not found");
                });
    }
}
