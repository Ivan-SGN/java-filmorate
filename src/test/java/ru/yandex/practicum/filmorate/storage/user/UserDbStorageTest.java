package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate.storage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser("login1");
        userStorage.createUser(testUser);
    }

    @Test
    void testCreateUser() {
        User user = createUser("login2");

        User created = userStorage.createUser(user);

        assertTrue(created.getId() > 0);
        assertEquals("login2", created.getLogin());
    }

    @Test
    void testGetUserById() {
        Optional<User> found = userStorage.getUser(testUser.getId());

        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getId());
    }

    @Test
    void testGetAllUsers() {
        userStorage.createUser(createUser("login2"));

        List<User> users = userStorage.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void testUpdateUser() {
        testUser.setName("updated");

        User updated = userStorage.updateUser(testUser).orElseThrow();

        assertEquals("updated", updated.getName());
        assertEquals(testUser.getId(), updated.getId());
    }

    @Test
    void testAddFriend() {
        User friend = userStorage.createUser(createUser("login2"));

        userStorage.addFriend(testUser.getId(), friend.getId());

        Collection<User> friends = userStorage.getFriends(testUser.getId());

        assertEquals(1, friends.size());
    }

    @Test
    void testRemoveFriend() {
        User friend = userStorage.createUser(createUser("login2"));

        userStorage.addFriend(testUser.getId(), friend.getId());
        userStorage.removeFriend(testUser.getId(), friend.getId());

        Collection<User> friends = userStorage.getFriends(testUser.getId());

        assertEquals(0, friends.size());
    }

    @Test
    void testGetCommonFriends() {
        User user2 = userStorage.createUser(createUser("login2"));
        User common = userStorage.createUser(createUser("login3"));

        userStorage.addFriend(testUser.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(testUser.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
    }

    @Test
    void testDeleteUser() {
        int userId = testUser.getId();

        userStorage.deleteUser(userId);

        Optional<User> deleted = userStorage.getUser(userId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void testDeleteUserCascadeFriends() {
        User friend = userStorage.createUser(createUser("login2"));

        userStorage.addFriend(testUser.getId(), friend.getId());
        userStorage.deleteUser(testUser.getId());

        Collection<User> friendsOfFriend = userStorage.getFriends(friend.getId());

        assertEquals(0, friendsOfFriend.size());
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
