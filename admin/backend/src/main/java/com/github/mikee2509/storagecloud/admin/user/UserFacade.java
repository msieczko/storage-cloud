package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.admin.domain.dto.NewUserDto;
import com.github.mikee2509.storagecloud.admin.domain.dto.UserDto;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor
public class UserFacade {
    private UserService userService;

    public List<UserDto> getAllUsers() {
        return userService.listUsers().stream()
                .map(UserDto::from)
                .collect(Collectors.toList());
    }

    public String register(NewUserDto newUserDto) {
        requireNonNull(newUserDto);
        return userService.register(newUserDto);
    }
}
