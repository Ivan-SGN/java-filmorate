package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserService {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    public User addUser(User user) {
        user.setId(nextId);
        users.put(nextId, user);
        nextId++;
        return user;
    }

    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }
}
