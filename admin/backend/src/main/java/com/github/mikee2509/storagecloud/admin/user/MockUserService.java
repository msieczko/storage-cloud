package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.admin.domain.dto.NewUserDto;
import com.github.mikee2509.storagecloud.proto.UserDetails;
import com.github.mikee2509.storagecloud.proto.UserRole;

import java.util.ArrayList;
import java.util.List;

class MockUserService implements UserService {
    private List<UserDetails> usersList;

    public MockUserService() {
        UserDetails alex = UserDetails.newBuilder()
                .setUsername("alex321")
                .setFirstName("Alex")
                .setLastName("Ferguson")
                .setRole(UserRole.USER)
                .setTotalSpace(1000)
                .setUsedSpace(850)
                .build();

        UserDetails sara = UserDetails.newBuilder()
                .setUsername("sara123")
                .setFirstName("Sara")
                .setLastName("Bouchard")
                .setRole(UserRole.USER)
                .setTotalSpace(1000)
                .setUsedSpace(150)
                .build();

        ArrayList<UserDetails> usersList = new ArrayList<>();
        usersList.add(alex);
        usersList.add(sara);
        this.usersList = usersList;
    }

    @Override
    public List<UserDetails> listUsers() {
        return usersList;
    }

    @Override
    public String register(NewUserDto newUserDto) {
        UserDetails newUserDetails = UserDetails.newBuilder()
                .setUsername(newUserDto.getUsername())
                .setFirstName(newUserDto.getFirstName())
                .setLastName(newUserDto.getLastName())
                .setRole(UserRole.USER)
                .setTotalSpace(1000)
                .setUsedSpace(300)
                .build();

        usersList.add(newUserDetails);
        return "OK";
    }
}
