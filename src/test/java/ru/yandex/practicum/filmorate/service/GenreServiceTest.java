package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class GenreServiceTest {

    private final GenreService genreService;

    @Autowired
    public GenreServiceTest(GenreService genreService) {
        this.genreService = genreService;
    }

    @Test
    void testGetAllGenres() {
        Collection<Genre> genres = genreService.getAllGenres();

        assertEquals(6, genres.size());
    }

    @Test
    void testGetGenreById() {
        Genre genre = genreService.getGenre(1);

        assertEquals(1, genre.getId());
    }

    @Test
    void testGetGenreNotFound() {
        assertThrows(
                ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> genreService.getGenre(999)
        );
    }
}