package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.admin.domain.dto.FileDto;
import com.github.mikee2509.storagecloud.admin.domain.dto.FilesRequestDto;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class FileFacade {
    private FileService fileService;

    public List<FileDto> getUserFilesFromPath(FilesRequestDto filesRequestDto) {
        return fileService.listFiles(filesRequestDto.getUsername(), filesRequestDto.getPath()).stream()
                .map(FileDto::from)
                .collect(Collectors.toList());
    }
}
