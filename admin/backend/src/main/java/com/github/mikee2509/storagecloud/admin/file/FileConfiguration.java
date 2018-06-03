package com.github.mikee2509.storagecloud.admin.file;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
class FileConfiguration {

    @Bean
    @Profile("mock")
    FileService mockFileService() {
        return new MockFileService();
    }

    @Bean
    @Profile("default")
    FileService tcpFileService() {
        return new TcpFileService();
    }

    @Bean
    FileFacade fileFacade(FileService fileService) {
        return new FileFacade(fileService);
    }
}
