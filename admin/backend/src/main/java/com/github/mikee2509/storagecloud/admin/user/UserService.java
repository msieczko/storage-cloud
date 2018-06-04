package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.proto.UserDetails;

import java.util.List;

interface UserService {
    List<UserDetails> listUsers();
}
