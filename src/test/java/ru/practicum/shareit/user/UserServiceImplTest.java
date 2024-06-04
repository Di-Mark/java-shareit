package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.config.PersistenceConfig;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestPropertySource(properties = { "db.name=test"})
@SpringJUnitConfig( { PersistenceConfig.class, UserServiceImpl.class})
public class UserServiceImplTest {
    private final EntityManager em;
    private final UserService service;

    @Test
    void createUserTest(){
        User user = makeUser("Пётр","some@email.com");
        service.createUser(user);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User result = query.setParameter("email", user.getEmail())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(user.getName()));
        assertThat(result.getEmail(), equalTo(user.getEmail()));
    }

    private User makeUser(String name, String email){
        User user  = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
