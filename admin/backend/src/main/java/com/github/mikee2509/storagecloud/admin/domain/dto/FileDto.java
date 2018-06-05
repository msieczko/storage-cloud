package com.github.mikee2509.storagecloud.admin.domain.dto;

import com.github.mikee2509.storagecloud.proto.File;
import com.github.mikee2509.storagecloud.proto.FileType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
public class FileDto {
    private String filename;
    private FileType fileType;
    private Long size;
    private String owner;
    private String ownerUsername;
    private LocalDateTime creationDate;
    private boolean isShared;

    public static FileDto from(File file) {
        return FileDto.builder()
                .filename(file.getFilename())
                .fileType(file.getFiletype())
                .size(file.getSize())
                .owner(file.getOwner())
                .ownerUsername(file.getOwnerUsername())
                .creationDate(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(file.getCreationDate()), ZoneId.systemDefault()))
                .isShared(file.getIsShared())
                .build();
    }
}
