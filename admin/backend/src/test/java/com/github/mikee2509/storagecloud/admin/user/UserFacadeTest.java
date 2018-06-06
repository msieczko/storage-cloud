package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.admin.domain.dto.NewUserDto;
import com.github.mikee2509.storagecloud.admin.domain.dto.UserDto;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@NoArgsConstructor
@ActiveProfiles("mock")
@RunWith(SpringRunner.class)
public class UserFacadeTest {
    @Autowired
    private UserFacade userFacade;

    @Test
    public void register() {
        NewUserDto newUserDto = NewUserDto.builder()
                .firstName("Michał")
                .lastName("Sieczkowski")
                .username("mikee2509")
                .password("mypass")
                .build();

        String register = userFacade.register(newUserDto);
        assertThat(register).isEqualTo("OK");

        List<UserDto> allUsers = userFacade.getAllUsers();
        Optional<UserDto> registeredUser = allUsers.stream()
                .filter(userDto -> Objects.equals(userDto.getUsername(), newUserDto.getUsername()))
                .findFirst();

        assertThat(registeredUser).isPresent();
        assertThat(registeredUser.get()).isEqualToIgnoringGivenFields(newUserDto, "capacity",
                "usedSpace", "password");
    }
}
