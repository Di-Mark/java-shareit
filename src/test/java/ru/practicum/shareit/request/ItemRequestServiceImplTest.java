package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {
    private final EntityManager em;
    private final UserService userService;
    private final ItemRequestService itemRequestService;

    @Test
    void createRequest() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequestService.createRequest(itemRequest, user.getId());
        TypedQuery<ItemRequest> query =
                em.createQuery("Select i from ItemRequest i where i.description = :description", ItemRequest.class);
        ItemRequest result = query.setParameter("description", itemRequest.getDescription())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(result.getCreated(), notNullValue());
    }


    @Test
    void findRequestById() {
        User user1 = userService.createUser(makeUser("Пётр", "some@email.com"));
        User user2 = userService.createUser(makeUser("new", "new@email.com"));
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequestService.createRequest(itemRequest, user1.getId());
        ItemRequestDto result = itemRequestService.findRequestById(user1.getId(), user2.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(result.getCreated(), notNullValue());
    }


    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
