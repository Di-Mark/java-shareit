package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    private final EntityManager em;
    private final UserService service;
    private final UserRepository userRepository;

    @Test
    void createUserTest() {
        User user = makeUser("Пётр", "some@email.com");
        service.createUser(user);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User result = query.setParameter("email", user.getEmail())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(user.getName()));
        assertThat(result.getEmail(), equalTo(user.getEmail()));
        Assertions.assertNotNull(userRepository.findAll());
    }

    @Test
    void createEqualsEmail() {
        User user = makeUser("Пётр", "some@email.com");
        User user2 = makeUser("Пётр", "some@email.com");
        service.createUser(user);
        RuntimeException e = Assertions.assertThrows(RuntimeException.class, () -> service.createUser(user2));
        Assertions.assertEquals(e.getMessage(), null);
    }

    @Test
    void createWithEmptyName() {
        User user = makeUser("", "some@email.com");
        ValidationException e = Assertions.assertThrows(ValidationException.class, () -> service.createUser(user));
        Assertions.assertEquals(e.getMessage(), "Имя пользователя не может быть пустым");
    }

    @Test
    void createWithFailEmail() {
        User user = makeUser("n", "someemail.com");
        ValidationException e = Assertions.assertThrows(ValidationException.class, () -> service.createUser(user));
        Assertions.assertEquals(e.getMessage(), "неправильный формат почты пользователя");
    }

    @Test
    void createEqualEmail() {
        User user1 = makeUser("Пётр", "some@email.com");
        User user2 = makeUser("Пётр", "some@email.com");
        service.createUser(user1);
    }

    @Test
    void findAllUsers() {
        List<User> sourceUsers = List.of(
                makeUser("Пётр", "some@email.com"),
                makeUser("2", "2some@email.com"),
                makeUser("3", "3some@email.com")
        );
        for (User user : sourceUsers) {
            em.persist(user);
        }
        em.flush();
        List<User> targetUsers = service.findAllUsers();
        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (User sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    @Test
    void patchUser() {
        User user = makeUser("Пётр", "some@email.com");
        User saveUser = service.createUser(user);
        User newUser = makeUser("Павел", "new@email.com");
        service.patchUser(newUser, saveUser.getId());
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User result = query.setParameter("email", newUser.getEmail())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(newUser.getName()));
        assertThat(result.getEmail(), equalTo(newUser.getEmail()));
    }

    @Test
    void patchUserWithFailName() {
        User user = makeUser("Пётр", "some@email.com");
        User saveUser = service.createUser(user);
        User newUser = makeUser("", "new@email.com");
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> service.patchUser(newUser, user.getId()));
        Assertions.assertEquals(e.getMessage(), "Имя пользователя не может быть пустым");
    }

    @Test
    void patchUserWithFailEmail() {
        User user = makeUser("Пётр", "some@email.com");
        User saveUser = service.createUser(user);
        User newUser = makeUser("n", "newemail.com");
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> service.patchUser(newUser, user.getId()));
        Assertions.assertEquals(e.getMessage(), "неправильный формат почты пользователя");
    }


    @Test
    void getUser() {
        User user = makeUser("Пётр", "some@email.com");
        User saveUser = service.createUser(user);

        User targetUser = service.getUser(saveUser.getId());
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User result = query.setParameter("email", targetUser.getEmail())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(targetUser.getName()));
        assertThat(result.getEmail(), equalTo(targetUser.getEmail()));
    }

    @Test
    void deleteUser() {
        User user = makeUser("Пётр", "some@email.com");
        User saveUser = service.createUser(user);
        service.deleteUser(saveUser.getId());
        List<User> targetUsers = service.findAllUsers();
        assertThat(targetUsers, hasSize(0));
    }


    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
