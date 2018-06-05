package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
class UserConfiguration {

    @Bean
    @Profile("mock")
    UserService mockUserService() {
        return new MockUserService();
    }

    @Bean
    @Profile("tcp")
    UserService tcpUserService(TcpClientService tcpClientService) {
        return new TcpUserService(tcpClientService);
    }

    @Bean
    UserFacade userFacade(UserService userService) {
        return new UserFacade(userService);
    }
}
