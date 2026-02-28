package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User createUser(User user);

    Optional<User> getUser(int id);

    List<User> getAllUsers();

    Optional<User> updateUser(User user);
}
