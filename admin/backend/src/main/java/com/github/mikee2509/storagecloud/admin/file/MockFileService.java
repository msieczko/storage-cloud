package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.proto.File;
import com.github.mikee2509.storagecloud.proto.FileType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class MockFileService implements FileService {
    private List<File> files;

    MockFileService() {
        File cat = File.newBuilder()
                .setFilename("crazy-cat.gif")
                .setFiletype(FileType.FILE)
                .setSize(1024)
                .setOwner("mikee2509")
                .setCreationDate(System.currentTimeMillis())
                .build();
        File photos = File.newBuilder()
                .setFilename("photos")
                .setFiletype(FileType.DIRECTORY)
                .setSize(6041)
                .setOwner("mikee2509")
                .setCreationDate(System.currentTimeMillis())
                .build();
        File dog = File.newBuilder()
                .setFilename("photos/doge.jpg")
                .setFiletype(FileType.FILE)
                .setSize(6041)
                .setOwner("mikee2509")
                .setCreationDate(System.currentTimeMillis())
                .build();

        files = Arrays.asList(cat, photos, dog);
    }

    @Override
    public List<File> listFiles(String username, String path) {
        return files.stream()
                .filter(file -> file.getOwner().equals(username))
                .filter(file -> {
                    if (path.isEmpty()) {
                        return !file.getFilename().contains("/");
                    } else {
                        return file.getFilename().startsWith(path + "/");
                    }
                })
                .collect(Collectors.toList());
    }
}
