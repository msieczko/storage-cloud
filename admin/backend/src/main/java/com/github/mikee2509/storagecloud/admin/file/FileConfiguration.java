package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
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
    @Profile("tcp")
    FileService tcpFileService(TcpClientService tcpClientService) {
        return new TcpFileService(tcpClientService);
    }

    @Bean
    FileFacade fileFacade(FileService fileService) {
        return new FileFacade(fileService);
    }
}
