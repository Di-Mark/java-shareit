package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.User;
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
public class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void createItem() {
        ItemDto itemDto = makeItemDto("name", "desc", true);
        userService.createUser(makeUser("Пётр", "some@email.com"));
        itemService.createItem(itemDto, 6L);
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item result = query.setParameter("name", itemDto.getName())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getName(), equalTo(itemDto.getName()));
        assertThat(result.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(result.getRequest(), equalTo(itemDto.getRequestId()));
        assertThat(result.getOwner(), equalTo(new User(6l, "Пётр", "some@email.com")));
    }

    @Test
    void getAllItems() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("name", "desc", true),
                makeItemDto("name2", "desc2", true),
                makeItemDto("name3", "desc3", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, 2l);
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
        userService.createUser(makeUser("Пётр", "some@email.com"));
        itemService.createItem(itemDto, 1L);
        ItemDto newItem = makeItemDto("new", "new", true);
        itemService.patchItem(newItem, 1L, 1L);
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.name = :name", Item.class);
        Item result = query.setParameter("name", newItem.getName())
                .getSingleResult();
        assertThat(result.getId(), notNullValue());
        assertThat(result.getAvailable(), equalTo(newItem.getAvailable()));
        assertThat(result.getRequest(), equalTo(newItem.getRequestId()));
        assertThat(result.getOwner(), equalTo(new User(1l, "Пётр", "some@email.com")));
    }

    @Test
    void getItem() {
        ItemDto itemDto = makeItemDto("name", "desc", true);
        userService.createUser(makeUser("Пётр", "some@email.com"));
        itemService.createItem(itemDto, 4L);
        List<ItemDto> items = itemService.getAllItems();
        ItemDtoBooking result = itemService.getItem(8L, 4l);
        assertThat(result.getId(), notNullValue());
        assertThat(result.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(result.getRequest(), equalTo(itemDto.getRequestId()));
    }

    @Test
    void getItemsListForUser() {
        userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("name", "desc", true),
                makeItemDto("name2", "desc2", true),
                makeItemDto("name3", "desc3", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, 5l);
        }
        List<ItemDtoBooking> targetItems = itemService.getItemsListForUser(5L, 0, 20);
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
        userService.createUser(makeUser("Пётр", "some@email.com"));
        List<ItemDto> sourceItems = List.of(
                makeItemDto("name", "desc", true),
                makeItemDto("name2", "desc2", true),
                makeItemDto("name3", "desc3", true)
        );
        for (ItemDto itemDto : sourceItems) {
            itemService.createItem(itemDto, 3l);
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
