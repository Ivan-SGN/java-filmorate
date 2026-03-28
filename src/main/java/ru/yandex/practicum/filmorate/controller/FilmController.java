package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.controller.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public FilmRsDto addFilm(@Valid @RequestBody FilmRqDto filmRqDto) {
        return filmService.addFilm(filmRqDto);
    }

    @PutMapping
    public FilmRsDto updateFilm(@Valid @RequestBody FilmRqDto filmRqDto) {
        validateUpdateId(filmRqDto.getId(), "Film");
        return filmService.updateFilm(filmRqDto);
    }

    @GetMapping
    public Collection<FilmRsDto> getFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public FilmRsDto getFilm(@PathVariable @Positive int id) {
        return filmService.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmRsDto> getPopular(@RequestParam(defaultValue = "10") @Positive Integer count) {
        return filmService.getPopular(count);
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable @Positive int filmId) {
        filmService.deleteFilm(filmId);
    }

    private void validateUpdateId(Long id, String entityName) {
        if (id == null || id <= 0) {
            throw new ValidationException(entityName + " id must be positive");
        }
    }
}
