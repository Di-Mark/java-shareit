package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemTest {

    @Test
    void equalTest() {
        Item item1 = new Item(1L, "name", "desc", true, null, null);
        Item item2 = new Item(1L, "name", "desc", true, null, null);
        assertEquals(item1.equals(item2), true);
        item1.hashCode();
    }
}
