package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.admin.domain.dto.FileDto;
import com.github.mikee2509.storagecloud.admin.domain.dto.FilesRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
class FileController {
    private FileFacade fileFacade;

    @GetMapping("/files")
    List<FileDto> getUserFilesFromPath(FilesRequestDto filesRequestDto) {
        return fileFacade.getUserFilesFromPath(filesRequestDto);
    }
}
