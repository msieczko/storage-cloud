package com.github.mikee2509.storagecloud.admin.domain.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UserAuthDto {
    private String username;
    private String password;
}
