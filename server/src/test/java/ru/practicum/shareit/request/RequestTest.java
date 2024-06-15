package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestTest {

    @Test
    void equalTest() {
        ItemRequest itemRequest1 = new ItemRequest(1L, "desc", null,
                LocalDateTime.of(2010, 12, 12, 12, 21, 12));
        ItemRequest itemRequest2 = new ItemRequest(1L, "desc", null,
                LocalDateTime.of(2010, 12, 12, 12, 21, 12));
        assertEquals(itemRequest1.equals(itemRequest2), true);
        itemRequest1.hashCode();
    }
}
