package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.controller.dto.DirectorDto;
import ru.yandex.practicum.filmorate.controller.dto.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectorServiceTest {

    @Mock
    private DirectorStorage storage;

    @Mock
    private DirectorMapper mapper;

    @InjectMocks
    private DirectorService service;

    @Test
    void getById_shouldReturnDto() {
        Director director = new Director();
        director.setId(1);
        director.setName("Nolan");

        DirectorDto dto = new DirectorDto().setId(1L).setName("Nolan");

        when(storage.getById(1)).thenReturn(Optional.of(director));
        when(mapper.mapToDto(director)).thenReturn(dto);

        DirectorDto result = service.getById(1);

        assertEquals("Nolan", result.getName());
        verify(storage).getById(1);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(storage.getById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.getById(1));
    }

    @Test
    void create_shouldReturnCreatedDto() {
        DirectorDto input = new DirectorDto().setName("Nolan");

        Director mapped = new Director();
        mapped.setName("Nolan");

        Director saved = new Director();
        saved.setId(1);
        saved.setName("Nolan");

        DirectorDto output = new DirectorDto().setId(1L).setName("Nolan");

        when(mapper.map(input)).thenReturn(mapped);
        when(storage.create(mapped)).thenReturn(saved);
        when(mapper.mapToDto(saved)).thenReturn(output);

        DirectorDto result = service.create(input);

        assertEquals(1L, result.getId());
    }

    @Test
    void delete_shouldCallStorage() {
        Director director = new Director();
        director.setId(1);
        director.setName("Nolan");

        when(storage.getById(1)).thenReturn(Optional.of(director));

        service.delete(1);

        verify(storage).delete(1);
    }
}