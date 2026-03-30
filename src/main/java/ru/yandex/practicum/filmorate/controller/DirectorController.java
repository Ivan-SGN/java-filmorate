package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.dto.DirectorDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Validated
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public List<DirectorDto> getAll() {
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public DirectorDto getById(@PathVariable @Positive int id) {
        return directorService.getById(id);
    }

    @PostMapping
    public DirectorDto create(@Valid @RequestBody DirectorDto dto) {
        return directorService.create(dto);
    }

    @PutMapping
    public DirectorDto update(@Valid @RequestBody DirectorDto dto) {
        return directorService.update(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive int id) {
        directorService.delete(id);
    }
}