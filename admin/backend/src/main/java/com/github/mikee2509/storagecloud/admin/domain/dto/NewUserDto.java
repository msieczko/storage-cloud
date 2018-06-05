package com.github.mikee2509.storagecloud.admin.domain.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class NewUserDto {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
}