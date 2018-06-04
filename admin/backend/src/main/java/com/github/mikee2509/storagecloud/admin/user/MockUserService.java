package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.proto.UserDetails;
import com.github.mikee2509.storagecloud.proto.UserRole;

import java.util.Arrays;
import java.util.List;

class MockUserService implements UserService {

    @Override
    public List<UserDetails> listUsers() {
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

        return Arrays.asList(alex, sara);
    }
}
