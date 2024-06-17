package ru.practicum.shareit.user.dto;

import lombok.Getter;

@Getter
public class UserDto {
    private String name;
    private String email;

    public UserDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
