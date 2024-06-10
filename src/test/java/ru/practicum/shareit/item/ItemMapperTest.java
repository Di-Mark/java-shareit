package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoBooking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemMapperTest {

    @Test
    void toItemDto() {
        Item item = new Item(1L, "name", "description", true,
                new User(2L,"shah","nen@"),
                new ItemRequest(3L,"kek",new User(4L,"num","num@"),
                        LocalDateTime.of(2020,6,6,6,6,6)));
        ItemDto itemDto = ItemMapper.toItemDto(item);
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
        assertEquals(item.getRequest().getId(),itemDto.getRequestId());
        assertEquals(item.getOwner(),new User(2L,"shah","nen@"));
    }

    @Test
    void toItem() {
        ItemDto itemDto = new ItemDto(1L, "yty", "aga", true,
                2L);
        Item item = ItemMapper.toItem(itemDto);
        item.setRequest(new ItemRequest(2L,"kek",new User(4L,"num","num@"),
                LocalDateTime.of(2022,8,8,8,8,8)));
        assertEquals(itemDto.getId(), item.getId());
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(itemDto.getRequestId(),item.getRequest().getId());
        assertEquals(itemDto.getAvailable(),item.getAvailable());
    }

    @Test
    void toItemDtoBooking() {
        Item item = new Item(1L, "wow", "wow", true,
                new User(4L,"Lol","lol@"),
                new ItemRequest(2L,"kek",new User(4L,"num","num@"),
                        LocalDateTime.of(2022,5,5,5,5,5)));
        ItemDtoBooking itemDtoBooking = ItemMapper.toItemDtoBooking(item);
        itemDtoBooking.setLastBooking(new BookingDtoForItem(3L,4L));
        itemDtoBooking.setNextBooking(new BookingDtoForItem(6L,7L));
        itemDtoBooking.setComments(List.of(new CommentDto(4L,"long",
                new Item(1L, "name", "description", true,
                        new User(2L,"shah","nen@"),
                        new ItemRequest(3L,"kek",new User(4L,"num","num@"),
                                LocalDateTime.of(2020,6,6,6,6,6))),
                new User(4L,"Lol","lol@"),
                LocalDateTime.of(2022,5,5,5,5,5),"namee"
                )));
        assertEquals(item.getId(), itemDtoBooking.getId());
        assertEquals(item.getName(), itemDtoBooking.getName());
        assertEquals(item.getDescription(), itemDtoBooking.getDescription());
        assertEquals(item.getRequest(),itemDtoBooking.getRequest());
        assertEquals(itemDtoBooking.getLastBooking(),new BookingDtoForItem(3L,4L));
        assertEquals(itemDtoBooking.getNextBooking(),new BookingDtoForItem(6L,7L));
        assertEquals(itemDtoBooking.getComments(),List.of(new CommentDto(4L,"long",
                new Item(1L, "name", "description", true,
                        new User(2L,"shah","nen@"),
                        new ItemRequest(3L,"kek",new User(4L,"num","num@"),
                                LocalDateTime.of(2020,6,6,6,6,6))),
                new User(4L,"Lol","lol@"),
                LocalDateTime.of(2022,5,5,5,5,5),"namee"
        )));


    }
}
