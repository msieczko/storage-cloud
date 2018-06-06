package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.proto.File;
import com.github.mikee2509.storagecloud.proto.FileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class MockFileService implements FileService {
    private List<File> files;
    private List<File> photos;
    private List<File> dogs;
    private List<File> cats;

    MockFileService() {
        File lorem = File.newBuilder()
                .setFilename("lorem-ipsum.txt")
                .setFiletype(FileType.FILE)
                .setSize(1012)
                .setOwner("alex321")
                .setCreationDate(System.currentTimeMillis())
                .build();
        File photos = File.newBuilder()
                .setFilename("photos")
                .setFiletype(FileType.DIRECTORY)
                .setSize(6041)
                .setOwner("alex321")
                .setCreationDate(System.currentTimeMillis())
                .build();

        File dogs = File.newBuilder()
                .setFilename("photos/dogs/")
                .setFiletype(FileType.DIRECTORY)
                .setSize(3014)
                .setOwner("alex321")
                .setCreationDate(System.currentTimeMillis())
                .build();

        File cats = File.newBuilder()
                .setFilename("photos/cats/")
                .setFiletype(FileType.DIRECTORY)
                .setSize(3027)
                .setOwner("alex321")
                .setCreationDate(System.currentTimeMillis())
                .build();

        File doge = File.newBuilder()
                .setFilename("photos/dogs/doge.jpg")
                .setFiletype(FileType.FILE)
                .setSize(2014)
                .setOwner("alex321")
                .setCreationDate(System.currentTimeMillis())
                .build();

        File husky = File.newBuilder()
                .setFilename("photos/dogs/husky.jpg")
                .setFiletype(FileType.FILE)
                .setSize(1000)
                .setOwner("alex321")
                .setCreationDate(System.currentTimeMillis())
                .build();

        File cat = File.newBuilder()
                .setFilename("photos/cats/crazy-cat.gif")
                .setFiletype(FileType.FILE)
                .setSize(3027)
                .setOwner("alex321")
                .setCreationDate(System.currentTimeMillis())
                .build();


        //lorem ipsum

        this.files = new ArrayList<>(Arrays.asList(cat, cats, doge, dogs, husky, lorem, photos));
        this.photos = new ArrayList<>(Arrays.asList(cats, dogs));
        this.dogs = new ArrayList<>(Arrays.asList(doge, husky));
        this.cats = new ArrayList<>(Collections.singletonList(cat));

    }

    @Override
    public String deleteUserFile(String username, String path) {
        dogs.remove(0);
        return "OK";
    }

    @Override
    public List<File> listUserFiles(String username, String path) {
        if (path.isEmpty()) {
            return getFiles(username, path, files);
        }
        if (path.equals("photos")) {
            return getFiles(username, path, photos);
        }
        if (path.equals("photos/dogs")) {
            return getFiles(username, path, dogs);
        }
        if (path.equals("photos/cats")) {
            return getFiles(username, path, cats);
        }
        return new ArrayList<>();
    }

    private List<File> getFiles(String username, String path, List<File> files) {
        return files.stream()
                .filter(file -> file.getOwner().equals(username))
                .filter(file -> {
                    if (path.isEmpty()) {
                        return !file.getFilename().contains("/");
                    } else {
                        return file.getFilename().startsWith(path + "/");
                    }
                })
                .map(file -> {
                    if (file.getFilename().contains("/")) {
                        String replace = file.getFilename().replace(path + "/", "");
                        int slash = replace.indexOf("/");
                        if (slash != -1) {
                            replace = replace.substring(0, slash);
                        }
                        return File.newBuilder(file)
                                .setFilename(replace)
                                .build();
                    } else {
                        return file;
                    }
                })
                .collect(Collectors.toList());
    }
}
