package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User createUser(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUser(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            return Optional.empty();
        }
        users.put(user.getId(), user);
        return Optional.of(user);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null) {
            user.getFriends().add(friendId);
            friend.getFriends().add(userId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
        }
    }

    @Override
    public Collection<User> getFriends(int userId) {
        User user = users.get(userId);
        if (user == null) {
            return List.of();
        }
        List<User> result = new ArrayList<>();
        for (Integer id : user.getFriends()) {
            User friend = users.get(id);
            if (friend != null) {
                result.add(friend);
            }
        }
        return result;
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        User user = users.get(userId);
        User other = users.get(otherId);
        if (user == null || other == null) {
            return List.of();
        }
        List<User> result = new ArrayList<>();
        for (Integer id : user.getFriends()) {
            if (other.getFriends().contains(id)) {
                User friend = users.get(id);
                if (friend != null) {
                    result.add(friend);
                }
            }
        }
        return result;
    }
}
