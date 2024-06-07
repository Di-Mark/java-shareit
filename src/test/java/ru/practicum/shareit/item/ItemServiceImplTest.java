package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.item.comment.dao.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;

    @Test
    void createItem() {
        ItemDto itemDto = makeItemDto("name", "desc", true);
        User userSave = userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemDto item = itemService.createItem(itemDto, userSave.getId());
        List<Item> items = itemRepository.findByOwner(userSave, PageRequest.of(0, 20)).getContent();
        Assertions.assertEquals(ItemMapper.toItemDto(items.get(0)), item);
        items = itemRepository.search("a", PageRequest.of(0, 20)).getContent();
        Assertions.assertEquals(ItemMapper.toItemDto(items.get(0)), item);
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item result = query.setParameter("name", itemDto.getName())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(result.getRequest(), equalTo(itemDto.getRequestId()));
        assertThat(result.getOwner(), equalTo(new User(userSave.getId(), "Пётр", "some@email.com")));
    }


    @Test
    void getAllItems() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("name", "desc", true),
                makeItemDto("name2", "desc2", true),
                makeItemDto("name3", "desc3", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, user.getId());
        }
        List<ItemDto> targetItems = itemService.getAllItems();
        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable()))
            )));
        }
    }

    @Test
    void patchItem() {
        ItemDto itemDto = makeItemDto("name", "desc", true);
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemDto item = itemService.createItem(itemDto, user.getId());
        ItemDto newItem = makeItemDto("new", "new", true);
        itemService.patchItem(newItem, item.getId(), user.getId());
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item result = query.setParameter("name", newItem.getName())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getAvailable(), equalTo(newItem.getAvailable()));
        assertThat(result.getRequest(), equalTo(newItem.getRequestId()));
        assertThat(result.getOwner(), equalTo(new User(user.getId(), "Пётр", "some@email.com")));
    }

    @Test
    void getItem() {
        ItemDto itemDto = makeItemDto("name", "desc", true);
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemDto item = itemService.createItem(itemDto, user.getId());
        ItemDtoBooking result = itemService.getItem(item.getId(), user.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(result.getRequest(), equalTo(itemDto.getRequestId()));
    }

    @Test
    void getItemsListForUser() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("name", "desc", true),
                makeItemDto("name2", "desc2", true),
                makeItemDto("name3", "desc3", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, user.getId());
        }
        List<ItemDtoBooking> targetItems = itemService.getItemsListForUser(user.getId(), 0, 20);
        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable()))
            )));
        }
    }

    @Test
    void searchItemsForText() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("name", "desc", true),
                makeItemDto("name2", "desc2", true),
                makeItemDto("name3", "desc3", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, user.getId());
        }
        List<ItemDto> targetItems = itemService.searchItemsForText("nAme", 0, 20);
        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable()))
            )));
        }
    }

    @Test
    void addComment() {
        User ow = userService.createUser(makeUser("Пётр", "some@email.com"));
        User book = userService.createUser(makeUser("booker", "booker@email.com"));
        ItemDto itemDto = makeItemDto("name", "desc", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 3, 6, 12, 12, 12),
                LocalDateTime.of(2024, 4, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        em.persist(booking);
        em.flush();
        Comment comment = new Comment();
        comment.setText("text");
        CommentDto result = itemService.addComment(comment, item.getId(), book.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getText(), equalTo(comment.getText()));
        assertThat(result.getItem().getId(), equalTo(item.getId()));
        List<Comment> comments = commentRepository.findByItem(itemRepository.findById(item.getId()).get());
        Assertions.assertNotNull(comments.get(0));
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

    private Booking makeBooking(LocalDateTime start, LocalDateTime end, User booker, Item item, StatusBooking status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(status);
        return booking;
    }
}
