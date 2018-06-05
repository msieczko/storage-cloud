package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.admin.domain.dto.NewUserDto;
import com.github.mikee2509.storagecloud.admin.domain.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
class UserController {
    private UserFacade userFacade;

    @GetMapping("/users")
    List<UserDto> getAllUsers() {
        return userFacade.getAllUsers();
    }

    @PostMapping("/users")
    String register(@RequestBody NewUserDto newUserDto) {
        return userFacade.register(newUserDto);
    }
}
