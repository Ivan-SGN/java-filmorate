package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate.storage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbc;
    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = createFilm();
        testFilm = filmStorage.createFilm(testFilm);
        jdbc.update("INSERT INTO users(id,email,login,name,birthday) VALUES (1,'test@mail.com','login','name','1990-01-01')");
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = filmStorage.getFilm(testFilm.getId());

        assertTrue(filmOptional.isPresent());
        assertEquals(testFilm.getId(), filmOptional.get().getId());
    }

    @Test
    void testGetAllFilms() {
        List<Film> films = filmStorage.getAllFilms();

        assertFalse(films.isEmpty());
    }

    @Test
    void testUpdateFilm() {
        testFilm.setName("Updated film");

        Film updated = filmStorage.updateFilm(testFilm).orElseThrow();

        assertEquals("Updated film", updated.getName());
        assertEquals(testFilm.getId(), updated.getId());
    }

    @Test
    void testCreateFilm() {
        Film film = createFilm();

        Film created = filmStorage.createFilm(film);

        assertTrue(created.getId() > 0);
        assertEquals("Test film", created.getName());
    }

    @Test
    void testAddLike() {
        filmStorage.addLike(testFilm.getId(), 1);

        List<Film> popular = filmStorage.getPopularFilms(10);

        assertEquals(testFilm.getId(), popular.getFirst().getId());
    }

    @Test
    void testGetPopularFilms() {
        filmStorage.addLike(testFilm.getId(), 1);

        List<Film> popular = filmStorage.getPopularFilms(10);

        assertFalse(popular.isEmpty());
    }

    @Test
    void testRemoveLike() {
        filmStorage.addLike(testFilm.getId(), 1);
        filmStorage.removeLike(testFilm.getId(), 1);

        List<Film> popular = filmStorage.getPopularFilms(10);

        assertNotNull(popular);
    }

    @Test
    void testDeleteFilm() {
        int filmId = testFilm.getId();

        filmStorage.deleteFilm(filmId);

        Optional<Film> deleted = filmStorage.getFilm(filmId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void testDeleteFilmCascadeLikes() {
        filmStorage.addLike(testFilm.getId(), 1);

        filmStorage.deleteFilm(testFilm.getId());

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ?",
                Integer.class,
                testFilm.getId()
        );

        assertEquals(0, count);
    }

    private Film createFilm() {
        Film film = new Film();
        film.setName("Test film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        return film;
    }
}
