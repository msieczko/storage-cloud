package com.github.mikee2509.storagecloud.admin.domain.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthStatusDto {
    boolean authenticated;
    UserDto current;

    public static AuthStatusDto notAuthenticated() {
        return new AuthStatusDto(false, null);
    }

    public static AuthStatusDto authenticated(UserDto userDto) {
        return new AuthStatusDto(true, userDto);
    }
}
