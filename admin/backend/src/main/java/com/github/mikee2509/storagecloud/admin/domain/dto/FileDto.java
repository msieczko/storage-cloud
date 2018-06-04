package com.github.mikee2509.storagecloud.admin.domain.dto;

import com.github.mikee2509.storagecloud.proto.File;
import com.github.mikee2509.storagecloud.proto.FileType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class FileDto {
    private String filename;
    private FileType fileType;
    private Long size;
    private String owner;
    private LocalDateTime creationDate;
    private List<ParamDto> sharedWith;

    public static FileDto from(File file) {
        return FileDto.builder()
                .filename(file.getFilename())
                .fileType(file.getFiletype())
                .size(file.getSize())
                .owner(file.getOwner())
                .creationDate(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(file.getCreationDate()), ZoneId.systemDefault()))
                .sharedWith(file.getSharedWithList().stream()
                        .map(ParamDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
