package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {
    @Test
    void userEqual() {
        User user1 = new User(1L, "name", "desc");
        User user2 = new User(1L, "name", "desc");
        assertEquals(user1.equals(user2), true);
        user1.hashCode();
    }
}
