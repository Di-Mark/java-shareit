package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImpl {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final ItemRequestService itemRequestService;

    @Test
    void createRequest() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        List<User> users = userService.findAllUsers();
        itemRequestService.createRequest(itemRequest, 3L);
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
        userService.createUser(makeUser("Пётр", "some@email.com"));
        userService.createUser(makeUser("new", "new@email.com"));
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        itemRequestService.createRequest(itemRequest, 1L);
        ItemRequestDto result = itemRequestService.findRequestById(1L, 2L);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(result.getCreated(), notNullValue());
    }

    private ItemDto makeItemDto(String name, String description, Boolean available) {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        return itemDto;
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}
