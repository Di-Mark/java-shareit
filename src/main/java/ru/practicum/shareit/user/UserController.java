package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dao.UserDao;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserDao userDao;

    @Autowired
    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping
    public List<User> findAllUsers() {
        return userDao.findAllUsers();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userDao.createUser(user);
    }

    @PatchMapping("/{id}")
    public User patchUser(@RequestBody User user, @PathVariable("id") Long id) {
        return userDao.patchUser(user, id);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") Long id) {
        return userDao.getUser(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        userDao.deleteUser(id);
    }
}
