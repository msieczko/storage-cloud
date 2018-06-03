package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.proto.File;
import com.github.mikee2509.storagecloud.proto.FileType;

import java.util.Arrays;
import java.util.List;

class MockFileService implements FileService {

    @Override
    public List<File> listFiles(String username, String path) {
        File cat = File.newBuilder()
                .setFilename("/crazy-cat.gif")
                .setFiletype(FileType.FILE)
                .setSize(1024)
                .setOwner(username)
                .setCreationDate(System.currentTimeMillis())
                .build();
        File photos = File.newBuilder()
                .setFilename("/photos")
                .setFiletype(FileType.DIRECTORY)
                .setSize(6041)
                .setOwner(username)
                .setCreationDate(System.currentTimeMillis())
                .build();

        return Arrays.asList(cat, photos);
    }
}
