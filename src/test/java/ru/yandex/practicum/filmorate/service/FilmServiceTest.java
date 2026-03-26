package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        Film film = filmService.addFilm(createFilm("Film"));
        film.setName("Updated");

        Film updated = filmService.updateFilm(film);

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
    void testReleaseDateBeforeMinimumThrowsException() {
        Film film = createFilm("OldFilm");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        org.junit.jupiter.api.Assertions.assertThrows(
                ru.yandex.practicum.filmorate.exception.ValidationException.class,
                () -> filmService.addFilm(film)
        );
    }

    @Test
    void testAddLikeAndGetPopularFilms() {
        User user = userService.addUser(createUser());

        Film film1 = filmService.addFilm(createFilm("Film1"));
        Film film2 = filmService.addFilm(createFilm("Film2"));

        filmService.addLike(film1.getId(), user.getId());

        List<Film> popular = filmService.getPopular(10);

        assertEquals(2, popular.size());
        assertEquals(film1.getId(), popular.getFirst().getId());
    }

    @Test
    void testRemoveLike() {
        User user = userService.addUser(createUser());
        Film film = filmService.addFilm(createFilm("Film"));

        filmService.addLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user.getId());

        assertEquals(0, filmService.getFilm(film.getId()).getLikes().size());
    }

    @Test
    void testGetFilmById() {
        Film film = filmService.addFilm(createFilm("Film"));

        assertEquals(film, filmService.getFilm(film.getId()));
    }

    @Test
    void testAddLikeTwiceDoesNotDuplicate() {
        User user = userService.addUser(createUser());
        Film film = filmService.addFilm(createFilm("Film"));

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user.getId());

        List<Film> popular = filmService.getPopular(10);
        assertEquals(film.getId(), popular.getFirst().getId());
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }

    private User createUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}