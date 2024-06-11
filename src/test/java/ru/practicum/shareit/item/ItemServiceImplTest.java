package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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
        ItemDto itemDto = makeItemDto("aka", "это", true);
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
    void createItemWithFailId() {
        ItemDto itemDto = makeItemDto("yttt", "cat", true);
        User userSave = userService.createUser(makeUser("Пётр", "some@email.com"));
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.createItem(itemDto, null));
        Assertions.assertEquals(e.getMessage(), "нет id владельца вещи");
    }

    @Test
    void createItemWithFailName() {
        ItemDto itemDto = makeItemDto("", "deeeeesk", true);
        User userSave = userService.createUser(makeUser("Пётр", "some@email.com"));
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.createItem(itemDto, userSave.getId()));
        Assertions.assertEquals(e.getMessage(), "имя вещи не может быть пустым или отсутствовать");
    }

    @Test
    void createItemWithFailDesc() {
        ItemDto itemDto = makeItemDto("ooeeee", "", true);
        User userSave = userService.createUser(makeUser("Пётр", "some@email.com"));
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.createItem(itemDto, userSave.getId()));
        Assertions.assertEquals(e.getMessage(), "описание вещи не может быть пустым или отсутствовать");
    }

    @Test
    void createItemWithFailStatus() {
        ItemDto itemDto = makeItemDto("aggaaa", "@e", null);
        User userSave = userService.createUser(makeUser("Пётр", "some@email.com"));
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.createItem(itemDto, userSave.getId()));
        Assertions.assertEquals(e.getMessage(), "статус вещи не может быть пустым или отсутствовать");
    }

    @Test
    void getAllItems() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("cao", "norm", true),
                makeItemDto("kak", "vvv", true),
                makeItemDto("lll", "ohoh", true)
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
        ItemDto itemDto = makeItemDto("nan", "dad", true);
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
    void patchItemWithFailIdUser() {
        ItemDto itemDto = makeItemDto("tyyt", "ne", true);
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemDto item = itemService.createItem(itemDto, user.getId());
        ItemDto newItem = makeItemDto("new", "new", true);
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.patchItem(newItem, item.getId(), null));
        Assertions.assertEquals(e.getMessage(), "отсутствует id владельца вещи");
    }

    @Test
    void patchItemWithFailName() {
        ItemDto itemDto = makeItemDto("owww", "owwow", true);
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemDto item = itemService.createItem(itemDto, user.getId());
        ItemDto newItem = makeItemDto("", "new", true);
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.patchItem(newItem, item.getId(), user.getId()));
        Assertions.assertEquals(e.getMessage(), "имя вещи не может быть пустым или отсутствовать");
    }

    @Test
    void patchItemWithFailDesc() {
        ItemDto itemDto = makeItemDto("not", "et", true);
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        ItemDto item = itemService.createItem(itemDto, user.getId());
        ItemDto newItem = makeItemDto("n", "", true);
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.patchItem(newItem, item.getId(), user.getId()));
        Assertions.assertEquals(e.getMessage(), "описание вещи не может быть пустым или отсутствовать");
    }

    @Test
    void patchItemWithFailOwner() {
        ItemDto itemDto = makeItemDto("meme", "mme", true);
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        User user2 = userService.createUser(makeUser("Пётр", "@email.com"));
        ItemDto item = itemService.createItem(itemDto, user.getId());
        ItemDto newItem = makeItemDto("n", "@a", true);
        NotFoundException e = Assertions.assertThrows(NotFoundException.class,
                () -> itemService.patchItem(newItem, item.getId(), user2.getId()));
        Assertions.assertEquals(e.getMessage(), "только владелец вещи может вносить изменения");
    }

    @Test
    void getItem() {
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
        booking = makeBooking(
                LocalDateTime.of(2024, 10, 6, 12, 12, 12),
                LocalDateTime.of(2024, 10, 7, 12, 12, 12),
                new User(book.getId(), "booker", "booker@email.com"),
                new Item(item.getId(), "name", "desc", true,
                        new User(ow.getId(), "Пётр", "some@email.com"), null),
                StatusBooking.WAITING);
        em.persist(booking);
        em.flush();
        ItemDtoBooking result = itemService.getItem(item.getId(), ow.getId());
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
    void getItemsListForUserWithFailPage() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("ннн", "ттт", true),
                makeItemDto("ккк", "ооо", true),
                makeItemDto("иии", "ггг", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, user.getId());
        }
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.getItemsListForUser(user.getId(), -1, 20));
        Assertions.assertEquals(e.getMessage(), "");
    }

    @Test
    void searchItemsForText() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("каак", "кеек", true),
                makeItemDto("куук", "иик", true),
                makeItemDto("ооок", "щщк", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, user.getId());
        }
        List<ItemDto> targetItems = itemService.searchItemsForText("к", 0, 20);
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
    void searchItemsForTextWithEmptyText() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("й", "ц", true),
                makeItemDto("ф", "ы", true),
                makeItemDto("я", "ч", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, user.getId());
        }
        List<ItemDto> targetItems = itemService.searchItemsForText("", 0, 20);
        assertThat(targetItems, hasSize(0));
    }

    @Test
    void searchItemsForTextWithFailPage() {
        User user = userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("й", "ц", true),
                makeItemDto("ф", "ы", true),
                makeItemDto("я", "ч", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, user.getId());
        }
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.searchItemsForText("n", -1, 20));
        Assertions.assertEquals(e.getMessage(), "");
    }


    @Test
    void addComment() {
        User ow = userService.createUser(makeUser("макс", "kak@email.com"));
        User book = userService.createUser(makeUser("вова", "vova@email.com"));
        ItemDto itemDto = makeItemDto("ммм", "ццц", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2024, 3, 6, 5, 5, 5),
                LocalDateTime.of(2024, 4, 7, 6, 6, 6),
                new User(book.getId(), "вова", "vova@email.com"),
                new Item(item.getId(), "ммм", "ццц", true,
                        new User(ow.getId(), "макс", "kak@email.com"), null),
                StatusBooking.WAITING);
        em.persist(booking);
        em.flush();
        Comment comment1 = new Comment();
        comment1.setText("текст");
        CommentDto result = itemService.addComment(comment1, item.getId(), book.getId());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getText(), equalTo(comment1.getText()));
        assertThat(result.getItem().getId(), equalTo(item.getId()));
        Comment comment = commentRepository.findById(result.getId()).get();
        Assertions.assertNotNull(comment);
        Assertions.assertEquals(comment.getText(), "текст");
        Assertions.assertEquals(comment.getItem(), new Item(item.getId(), "ммм", "ццц", true,
                new User(ow.getId(), "макс", "kak@email.com"), null));
        Assertions.assertEquals(comment.getAuthor(), new User(book.getId(), "вова", "vova@email.com"));
        Assertions.assertNotNull(comment.getCreated());
    }

    @Test
    void addCommentWithFailText() {
        User ow = userService.createUser(makeUser("карл", "карл@email.com"));
        User book = userService.createUser(makeUser("петя", "петя@email.com"));
        ItemDto itemDto = makeItemDto("ааа", "ууу", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Booking booking = makeBooking(
                LocalDateTime.of(2022, 2, 6, 1, 1, 1),
                LocalDateTime.of(2023, 4, 7, 10, 10, 10),
                new User(book.getId(), "петя", "петя@email.com"),
                new Item(item.getId(), "ааа", "ууу", true,
                        new User(ow.getId(), "карл", "карл@email.com"), null),
                StatusBooking.WAITING);
        em.persist(booking);
        em.flush();
        Comment comment = new Comment();
        comment.setText("");
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.addComment(comment, item.getId(), book.getId()));
        Assertions.assertEquals(e.getMessage(), "");
    }

    @Test
    void addCommentWithFailBooing() {
        User ow = userService.createUser(makeUser("молод", "молод@email.com"));
        User book = userService.createUser(makeUser("стар", "стар@email.com"));
        ItemDto itemDto = makeItemDto("нет", "да", true);
        ItemDto item = itemService.createItem(itemDto, ow.getId());
        Comment comment = new Comment();
        comment.setText("t");
        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> itemService.addComment(comment, item.getId(), book.getId()));
        Assertions.assertEquals(e.getMessage(), "");
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
