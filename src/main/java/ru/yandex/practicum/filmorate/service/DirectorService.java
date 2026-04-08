package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;
    private final DirectorMapper directorMapper;

    public List<DirectorDto> getAll() {
        log.info("Get all directors request");
        return directorStorage.getAll().stream()
                .map(directorMapper::mapToDto)
                .toList();
    }

    public DirectorDto getById(int id) {
        Director director = getDirectorOrThrow(id);
        log.info("Director fetched: id={}", id);
        return directorMapper.mapToDto(director);
    }

    public DirectorDto create(DirectorDto dto) {
        Director director = directorStorage.create(directorMapper.map(dto));
        log.info("Director created: id={}", director.getId());
        return directorMapper.mapToDto(director);
    }

    public DirectorDto update(DirectorDto dto) {
        getDirectorOrThrow(dto.getId());
        Director director = directorMapper.map(dto);
        Director updated = directorStorage.update(director);
        log.info("Director updated: id={}", updated.getId());
        return directorMapper.mapToDto(updated);
    }

    public void delete(long id) {
        getDirectorOrThrow(id);
        directorStorage.delete(id);
        log.info("Director deleted: id={}", id);
    }

    private Director getDirectorOrThrow(long id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> {
                    log.warn("Director not found: id={}", id);
                    return new NotFoundException("Director not found with id=" + id);
                });
    }
}