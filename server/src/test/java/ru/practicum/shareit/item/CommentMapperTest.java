package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

public class CommentMapperTest {
    @Test
    void toCommentDto() {
        Comment comment = new Comment(3L, "short", new Item(1L, "not", "notid", true,
                new User(4L, "aaa", "aaa@"),
                new ItemRequest(2L, "kak", new User(4L, "net", "net@"),
                        LocalDateTime.of(2022, 5, 5, 5, 5, 5))),
                new User(4L, "vova", "vova@"),
                LocalDateTime.of(2016, 8, 8, 8, 8, 8));
        CommentDto commentDto = CommentMapper.toCommentDto(comment);
        Assertions.assertEquals(comment.getId(), commentDto.getId());
        Assertions.assertEquals(comment.getItem(), commentDto.getItem());
        Assertions.assertEquals(comment.getCreated(), commentDto.getCreated());
        Assertions.assertEquals(comment.getText(), commentDto.getText());
        Assertions.assertEquals(comment.getAuthor(), commentDto.getAuthor());
        Assertions.assertEquals(comment.getAuthor().getName(), commentDto.getAuthorName());
    }
}
