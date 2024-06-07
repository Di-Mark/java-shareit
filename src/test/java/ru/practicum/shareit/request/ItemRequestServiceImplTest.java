package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

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
public class ItemRequestServiceImplTest {
    private final EntityManager em;
    private final UserService userService;
    private final ItemRequestService itemRequestService;
    private final ItemRequestRepository itemRequestRepository;

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
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        ItemRequestDto itemRequestSave = itemRequestService.createRequest(itemRequest, user1.getId());
        ItemRequestDto result = itemRequestService.findRequestById(itemRequestSave.getId(), user1.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(result.getCreated(), notNullValue());
        List<ItemRequest> requests = itemRequestRepository.findAll(PageRequest.of(0, 20)).getContent();
        Assertions.assertNotNull(requests);
        requests = itemRequestRepository.findByRequestorOrderByCreatedDesc(user1);
        Assertions.assertNotNull(requests);
    }

    @Test
    void getRequestForUser() {
        User user1 = userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        ItemRequestDto itemRequestSave = itemRequestService.createRequest(itemRequest, user1.getId());
        List<ItemRequestDto> result = itemRequestService.getRequestForUser(user1.getId());
        assertThat(result.get(0).getId(), notNullValue());
        assertThat(result.get(0).getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(result.get(0).getCreated(), notNullValue());
    }

    @Test
    void findAllRequest() {
        User user1 = userService.createUser(makeUser("Пётр", "some@email.com"));
        User user2 = userService.createUser(makeUser("jo", "@email.com"));
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("описание");
        ItemRequestDto itemRequestSave = itemRequestService.createRequest(itemRequest, user1.getId());
        List<ItemRequestDto> result = itemRequestService.findAllRequest(0, 20, user2.getId());
        assertThat(result.get(0).getId(), notNullValue());
        assertThat(result.get(0).getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(result.get(0).getCreated(), notNullValue());
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

}
