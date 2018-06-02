package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.proto.User;
import com.github.mikee2509.storagecloud.proto.UserRole;

import java.util.Arrays;
import java.util.List;

class MockUserService implements UserService {

    @Override
    public List<User> listUsers() {
        User alex = User.newBuilder()
                .setUsername("alex321")
                .setFirstName("Alex")
                .setLastName("Ferguson")
                .setRole(UserRole.USER)
                .setCapacity(1000)
                .setUsedSpace(850)
                .build();

        User sara = User.newBuilder()
                .setUsername("sara123")
                .setFirstName("Sara")
                .setLastName("Bouchard")
                .setRole(UserRole.USER)
                .setCapacity(1000)
                .setUsedSpace(150)
                .build();

        return Arrays.asList(alex, sara);
    }
}
