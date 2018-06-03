package com.github.mikee2509.storagecloud.admin.domain.dto;

import com.github.mikee2509.storagecloud.proto.File;
import com.github.mikee2509.storagecloud.proto.FileType;
import com.github.mikee2509.storagecloud.proto.Param;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileDto {
    private String filename;
    private FileType fileType;
    private Long size;
    private String owner;
    private Long creationDate;
    private List<Param> metadata;

    public static FileDto from(File file) {
        return FileDto.builder()
                .filename(file.getFilename())
                .fileType(file.getFiletype())
                .size(file.getSize())
                .owner(file.getOwner())
                .creationDate(file.getCreationDate())
                .metadata(file.getMetadataList())
                .build();
    }
}
