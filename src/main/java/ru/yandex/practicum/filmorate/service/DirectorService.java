package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.dto.DirectorDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Slf4j
@Service
public class DirectorService {

    private final DirectorStorage directorStorage;
    private final DirectorMapper directorMapper;

    public DirectorService(DirectorStorage directorStorage, DirectorMapper directorMapper) {
        this.directorStorage = directorStorage;
        this.directorMapper = directorMapper;
    }

    public List<DirectorDto> getAll() {
        log.info("Get all directors");
        return directorStorage.getAll().stream()
                .map(directorMapper::mapToDto)
                .toList();
    }

    public DirectorDto getById(int id) {
        return directorMapper.mapToDto(getOrThrow(id));
    }

    public DirectorDto create(DirectorDto dto) {
        Director director = directorMapper.map(dto);
        return directorMapper.mapToDto(directorStorage.create(director));
    }

    public DirectorDto update(DirectorDto dto) {
        Director director = directorMapper.map(dto);
        getOrThrow(director.getId());
        return directorMapper.mapToDto(
                directorStorage.update(director)
                        .orElseThrow(() -> new IllegalStateException("Update failed"))
        );
    }

    public void delete(int id) {
        getOrThrow(id);
        directorStorage.delete(id);
    }

    private Director getOrThrow(int id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Director not found, id={}", id);
                    return new NotFoundException("Director not found");
                });
    }
}