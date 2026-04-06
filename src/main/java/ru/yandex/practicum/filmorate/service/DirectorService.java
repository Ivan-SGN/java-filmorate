package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.dto.DirectorDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;
    private final DirectorMapper directorMapper;

    public List<DirectorDto> getAll() {
        return directorStorage.getAll().stream()
                .map(directorMapper::mapToDto)
                .toList();
    }

    public DirectorDto getById(int id) {
        return directorMapper.mapToDto(
                directorStorage.getById(id)
                        .orElseThrow(() -> new NotFoundException("Director not found"))
        );
    }

    public DirectorDto create(DirectorDto dto) {
        return directorMapper.mapToDto(
                directorStorage.create(directorMapper.map(dto))
        );
    }

    public DirectorDto update(DirectorDto dto) {
        return directorMapper.mapToDto(
                directorStorage.update(directorMapper.map(dto))
        );
    }

    public void delete(int id) {
        directorStorage.delete(id);
    }
}