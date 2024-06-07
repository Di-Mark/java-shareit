package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.comment.model.Comment;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentTest {

    @Test
    void equalsTest() {
        Comment comment1 = new Comment(1L, "text", null, null, null);
        Comment comment2 = new Comment(1L, "text", null, null, null);
        assertEquals(comment1.equals(comment2), true);
        comment1.hashCode();
    }
}
