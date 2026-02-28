package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserServiceTest {

    @Test
    void testAddUser() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);

        User user = userService.addUser(createUser("login1"));

        assertEquals(1, user.getId());
        assertEquals("login1", user.getLogin());
    }

    @Test
    void testUpdateUser() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);

        User user = userService.addUser(createUser("login1"));
        user.setName("updated");
        User updated = userService.updateUser(user);

        assertEquals("updated", updated.getName());
        assertEquals(user.getId(), updated.getId());
    }

    @Test
    void testGetUser() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);

        User user = userService.addUser(createUser("login1"));
        User found = userService.getUser(user.getId());

        assertEquals(user.getId(), found.getId());
        assertEquals("login1", found.getLogin());
    }

    @Test
    void testGetAllUsers() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);

        userService.addUser(createUser("login1"));
        userService.addUser(createUser("login2"));

        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    void testAddFriendIsMutual() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);

        User user1 = userService.addUser(createUser("login1"));
        User user2 = userService.addUser(createUser("login2"));
        userService.addFriend(user1.getId(), user2.getId());

        assertEquals(1, userService.getFriends(user1.getId()).size());
        assertEquals(1, userService.getFriends(user2.getId()).size());
    }

    @Test
    void testRemoveFriendIsMutual() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        User user1 = userService.addUser(createUser("login1"));
        User user2 = userService.addUser(createUser("login2"));

        userService.addFriend(user1.getId(), user2.getId());
        userService.removeFriend(user1.getId(), user2.getId());

        assertEquals(0, userService.getFriends(user1.getId()).size());
        assertEquals(0, userService.getFriends(user2.getId()).size());
    }

    @Test
    void testGetCommonFriends() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        User user1 = userService.addUser(createUser("login1"));
        User user2 = userService.addUser(createUser("login2"));
        User common = userService.addUser(createUser("login3"));

        userService.addFriend(user1.getId(), common.getId());
        userService.addFriend(user2.getId(), common.getId());
        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(common.getId(), commonFriends.getFirst().getId());
    }

    @Test
    void testAddFriendTwiceDoesNotDuplicate() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        User user1 = userService.addUser(createUser("login1"));
        User user2 = userService.addUser(createUser("login2"));

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user2.getId());

        assertEquals(1, userService.getFriends(user1.getId()).size());
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}