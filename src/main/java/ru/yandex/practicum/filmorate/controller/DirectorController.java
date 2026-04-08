package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.dto.DirectorDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService service;

    @GetMapping
    public List<DirectorDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public DirectorDto get(@PathVariable int id) {
        return service.getById(id);
    }

    @PostMapping
    public DirectorDto create(@RequestBody DirectorDto dto) {
        return service.create(dto);
    }

    @PutMapping
    public DirectorDto update(@RequestBody DirectorDto dto) {
        return service.update(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}