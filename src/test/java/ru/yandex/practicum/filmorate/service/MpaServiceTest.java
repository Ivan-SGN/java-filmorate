package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.controller.dto.MpaDto;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class MpaServiceTest {

    private final MpaService mpaService;

    @Autowired
    public MpaServiceTest(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @Test
    void testGetAllMpa() {
        Collection<MpaDto> mpaList = mpaService.getAllMpa();

        assertEquals(5, mpaList.size());
    }

    @Test
    void testGetMpaById() {
        MpaDto mpa = mpaService.getMpa(1);

        assertEquals(1L, mpa.getId());
    }

    @Test
    void testGetMpaNotFound() {
        assertThrows(
                ru.yandex.practicum.filmorate.exception.NotFoundException.class,
                () -> mpaService.getMpa(999)
        );
    }
}
