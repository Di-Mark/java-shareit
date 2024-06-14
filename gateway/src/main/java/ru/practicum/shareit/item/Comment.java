package ru.practicum.shareit.item;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long id;
    private String text;
    private LocalDateTime created;

}
