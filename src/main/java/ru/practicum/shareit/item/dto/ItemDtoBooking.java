package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDtoBooking {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private ItemRequest request;
    private BookingDtoForItem nextBooking;
    private BookingDtoForItem lastBooking;
    private List<CommentDto> comments;
}
