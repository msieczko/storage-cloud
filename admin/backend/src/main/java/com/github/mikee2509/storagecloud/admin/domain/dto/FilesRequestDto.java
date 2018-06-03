package com.github.mikee2509.storagecloud.admin.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FilesRequestDto {
    private String username;
    private String path;
}
