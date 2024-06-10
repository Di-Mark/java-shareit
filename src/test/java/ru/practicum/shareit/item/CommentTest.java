package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentTest {

    @Test
    void equalsTest() {
        Comment comment1 = new Comment(1L, "text",
                new Item(3L,"name","d",true,new User(1L,"kak","kak@"),
                        new ItemRequest(5L,"d",new User(2L,"lol","lol@"),
                                LocalDateTime.of(2010, 7, 6, 5, 4, 3))),
                new User(7L,"wow","wow@"),
                LocalDateTime.of(2015, 5, 5, 5, 5, 5));
        Comment comment2 = new Comment(1L, "text",
                new Item(3L,"name","d",true,new User(1L,"kak","kak@"),
                        new ItemRequest(5L,"d",new User(2L,"lol","lol@"),
                                LocalDateTime.of(2010, 7, 6, 5, 4, 3))),
                new User(7L,"wow","wow@"),
                LocalDateTime.of(2015, 5, 5, 5, 5, 5));
        assertEquals(comment1, comment2);
        assertEquals(comment1.hashCode(),comment2.hashCode());
        assertEquals(comment1.equals(comment2),true);
    }
}
