package com.github.mikee2509.storagecloud.admin.domain.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class NewUserDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}