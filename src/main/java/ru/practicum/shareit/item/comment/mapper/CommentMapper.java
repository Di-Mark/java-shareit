package ru.practicum.shareit.item.comment.mapper;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setItem(comment.getItem());
        commentDto.setAuthor(comment.getAuthor());
        commentDto.setCreated(comment.getCreated());
        commentDto.setAuthorName(comment.getAuthor().getName());
        return commentDto;
    }
}
