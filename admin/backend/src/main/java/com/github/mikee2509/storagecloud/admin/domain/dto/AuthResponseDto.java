package com.github.mikee2509.storagecloud.admin.domain.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthResponseDto {
    private boolean success;
    private String errorMessage;

    public static AuthResponseDto success() {
        return new AuthResponseDto(true, null);
    }

    public static AuthResponseDto error(String message) {
        return new AuthResponseDto(false, message);
    }
}
