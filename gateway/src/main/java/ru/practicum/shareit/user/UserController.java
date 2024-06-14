package ru.practicum.shareit.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exeption.ValidationException;

import java.util.List;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> findAllUsers() {
        log.info("findAllUsers");
        return userClient.findAllUsers();
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody User user) {
        if (user.getName() == null || user.getName().equals("")) {
            log.info("Имя пользователя не может быть пустым");
            throw new ValidationException("Имя пользователя не может быть пустым");
        } else if (user.getEmail() == null || !user.getEmail().contains("@")) {
            log.info("неправильный формат почты пользователя");
            throw new ValidationException("неправильный формат почты пользователя");
        }
        log.info("create User");
        return userClient.createUser(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> patchUser(@RequestBody User user, @PathVariable("id") Long id) {
        if (user.getName() != null) {
            if (user.getName().equals("")) {
                log.info("Имя пользователя не может быть пустым");
                throw new ValidationException("Имя пользователя не может быть пустым");
            }
        }
        if (user.getEmail() != null) {
            if (!user.getEmail().contains("@")) {
                log.info("неправильный формат почты пользователя");
                throw new ValidationException("неправильный формат почты пользователя");
            }
        }
        log.info("patch User");
        return userClient.patchUser(user,id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable("id") Long id) {
        log.info("get User");
        return userClient.getUser(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("id") Long id) {
        log.info("delete User");
        return userClient.deleteUser(id);
    }

}
