package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.controller.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class FilmServiceTest {

    private final FilmService filmService;
    private final UserService userService;

    @Autowired
    public FilmServiceTest(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    @Test
    void testUpdateFilm() {
        FilmRsDto film = filmService.addFilm(createFilm("Film"));
        film.setName("Updated");

        FilmRsDto updated = filmService.updateFilm(toRequestDto(film));

        assertEquals("Updated", updated.getName());
        assertEquals(film.getId(), updated.getId());
    }

    @Test
    void testGetAllFilms() {
        filmService.addFilm(createFilm("Film1"));
        filmService.addFilm(createFilm("Film2"));

        assertEquals(2, filmService.getAllFilms().size());
    }

    @Test
    void testDeleteFilm() {
        FilmRsDto film = filmService.addFilm(createFilm("Film"));

        filmService.deleteFilm(film.getId().intValue());

        assertThrows(NotFoundException.class,
                () -> filmService.getFilm(film.getId().intValue()));
    }

    @Test
    void testDeleteNonExistingFilm() {
        assertThrows(NotFoundException.class,
                () -> filmService.deleteFilm(999999));
    }

    @Test
    void testAddLikeAndGetPopularFilms() {
        UserDto user = userService.addUser(createUser());

        FilmRsDto film1 = filmService.addFilm(createFilm("Film1"));
        FilmRsDto film2 = filmService.addFilm(createFilm("Film2"));

        filmService.addLike(film1.getId().intValue(), user.getId().intValue());

        List<FilmRsDto> popular = filmService.getPopular(10);

        assertEquals(2, popular.size());
        assertEquals(film1.getId(), popular.getFirst().getId());
    }

    @Test
    void testRemoveLike() {
        UserDto user = userService.addUser(createUser());
        FilmRsDto film1 = filmService.addFilm(createFilm("Film1"));
        FilmRsDto film2 = filmService.addFilm(createFilm("Film2"));

        filmService.addLike(film1.getId().intValue(), user.getId().intValue());
        filmService.removeLike(film1.getId().intValue(), user.getId().intValue());
        filmService.addLike(film2.getId().intValue(), user.getId().intValue());

        assertEquals(film2.getId(), filmService.getPopular(10).getFirst().getId());
    }

    @Test
    void testGetFilmById() {
        FilmRsDto film = filmService.addFilm(createFilm("Film"));

        assertEquals(film.getId(), filmService.getFilm(film.getId().intValue()).getId());
        assertEquals(film.getName(), filmService.getFilm(film.getId().intValue()).getName());
    }

    @Test
    void testAddLikeTwiceDoesNotDuplicate() {
        UserDto user = userService.addUser(createUser());
        FilmRsDto film = filmService.addFilm(createFilm("Film"));

        filmService.addLike(film.getId().intValue(), user.getId().intValue());
        filmService.addLike(film.getId().intValue(), user.getId().intValue());

        List<FilmRsDto> popular = filmService.getPopular(10);
        assertEquals(film.getId(), popular.getFirst().getId());
    }

    @Test
    void testAddFilmReturnsSortedGenresWithNames() {
        FilmRqDto filmRqDto = createFilm("Film");
        filmRqDto.setMpa(new IdDto().setId(3L));
        filmRqDto.setGenres(orderedGenreIds(5L, 3L));

        FilmRsDto created = filmService.addFilm(filmRqDto);

        assertEquals(List.of(3L, 5L), genreIds(created));
        assertEquals(List.of("Мультфильм", "Документальный"), genreNames(created));
        assertEquals(3L, created.getMpa().getId());
        assertEquals("PG-13", created.getMpa().getName());
    }

    @Test
    void testUpdateFilmReturnsSortedGenresWithNames() {
        FilmRsDto created = filmService.addFilm(createFilm("Film"));
        FilmRqDto updateRequest = toRequestDto(created);
        updateRequest.setMpa(new IdDto().setId(4L));
        updateRequest.setGenres(orderedGenreIds(6L, 2L));

        FilmRsDto updated = filmService.updateFilm(updateRequest);

        assertEquals(List.of(2L, 6L), genreIds(updated));
        assertEquals(List.of("Драма", "Боевик"), genreNames(updated));
        assertEquals(4L, updated.getMpa().getId());
        assertEquals("R", updated.getMpa().getName());
    }

    private FilmRqDto createFilm(String name) {
        FilmRqDto film = new FilmRqDto();
        film.setName(name);
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }

    private FilmRqDto toRequestDto(FilmRsDto film) {
        FilmRqDto filmRqDto = new FilmRqDto();
        filmRqDto.setId(film.getId());
        filmRqDto.setName(film.getName());
        filmRqDto.setDescription(film.getDescription());
        filmRqDto.setReleaseDate(film.getReleaseDate());
        filmRqDto.setDuration(film.getDuration());
        return filmRqDto;
    }

    private UserDto createUser() {
        UserDto user = new UserDto();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private Set<IdDto> orderedGenreIds(Long... ids) {
        Set<IdDto> genres = new LinkedHashSet<>();
        for (Long id : ids) {
            genres.add(new IdDto().setId(id));
        }
        return genres;
    }

    private List<Long> genreIds(FilmRsDto film) {
        return film.getGenres().stream().map(GenreDto::getId).toList();
    }

    private List<String> genreNames(FilmRsDto film) {
        return film.getGenres().stream().map(GenreDto::getName).toList();
    }
}