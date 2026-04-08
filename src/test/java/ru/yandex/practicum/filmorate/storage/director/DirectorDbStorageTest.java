package ru.yandex.practicum.filmorate.storage.director;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
class DirectorDbStorageTest {

    @Autowired
    private DirectorDbStorage storage;

    @Test
    void shouldCreateAndFindDirector() {
        Director director = new Director();
        director.setName("Nolan");

        Director saved = storage.create(director);

        Optional<Director> found = storage.getById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Nolan", found.get().getName());
    }
}