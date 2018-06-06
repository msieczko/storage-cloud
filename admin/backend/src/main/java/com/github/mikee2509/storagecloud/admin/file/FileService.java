package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.proto.File;

import java.util.List;

interface FileService {
    List<File> listUserFiles(String username, String path);
    String deleteUserFile(String username, String path);
}
