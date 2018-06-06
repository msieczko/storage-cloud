package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.admin.domain.dto.FileDto;
import lombok.AllArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
class FileController {
    private FileFacade fileFacade;

    private static String extractPath(final HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(pattern, path);
    }

    @GetMapping("/files/{username}/**")
    List<FileDto> getUserFilesFromPath(@PathVariable @NotBlank String username, HttpServletRequest request) {
        String path = extractPath(request);
        return fileFacade.getUserFilesFromPath(username, path);
    }

    @GetMapping("/delete/{username}/**")
    String deleteFile(@PathVariable @NotBlank String username, HttpServletRequest request) {
        String path = extractPath(request);
        return fileFacade.deleteFile(username, path);
    }
}
