package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Validated
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public Collection<MpaDto> getMpa() {
        return mpaService.getAllMpa();
    }

    @GetMapping("/{mpaId}")
    public MpaDto getMpa(@PathVariable @Positive int mpaId) {
        return mpaService.getMpa(mpaId);
    }
}
