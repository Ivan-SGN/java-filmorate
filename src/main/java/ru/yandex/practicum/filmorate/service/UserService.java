package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserService {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    public User addUser(User user) {
        validateLogin(user);
        normalizeName(user);
        user.setId(nextId);
        users.put(nextId, user);
        log.info("User added: id={}, login={}", user.getId(), user.getLogin());
        nextId++;
        return user;
    }

    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("User not found");
        }
        validateLogin(user);
        normalizeName(user);
        users.put(user.getId(), user);
        log.info("User updated: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }

    private void validateLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Login must not contain spaces");
        }
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}