package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.admin.domain.dto.FileDto;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor
public class FileFacade {
    private FileService fileService;

    public List<FileDto> getUserFilesFromPath(String username, String path) {
        requireNonNull(username, path);
        return fileService.listUserFiles(username, path).stream()
                .map(FileDto::from)
                .collect(Collectors.toList());
    }

    public String deleteFile(String username, String path) {
        requireNonNull(username, path);
        return fileService.deleteUserFile(username, path);
    }
}
