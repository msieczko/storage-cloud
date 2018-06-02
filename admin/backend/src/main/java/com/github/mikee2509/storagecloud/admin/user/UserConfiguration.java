package com.github.mikee2509.storagecloud.admin.user;

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

    UserService tcpUserService() {
        return new TcpUserService();
    }

    @Bean
    UserFacade userFacade(UserService userService) {
        return new UserFacade(userService);
    }
}
