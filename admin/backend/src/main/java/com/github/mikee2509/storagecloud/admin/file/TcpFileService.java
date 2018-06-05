package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
import com.github.mikee2509.storagecloud.admin.client.TcpService;
import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.exception.FileServiceException;
import com.github.mikee2509.storagecloud.proto.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.mikee2509.storagecloud.admin.domain.ParamId.PATH;
import static com.github.mikee2509.storagecloud.admin.domain.ParamId.USERNAME;

class TcpFileService extends TcpService implements FileService {
    @Value("${tin.hashAlgorithm}")
    private HashAlgorithm hAlgorithm;
    @Value("${tin.encryptionAlgorithm}")
    private EncryptionAlgorithm eAlgorithm;

    public TcpFileService(TcpClientService tcpClientService) {
        super(tcpClientService);
    }

    @Override
    public List<File> listFiles(String username, String path) {
        path = "/" + path;
        Packet listFilesPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.LIST_USER_FILES)
                .addParam(USERNAME.toString()).ofValue(username)
                .addParam(PATH.toString()).ofValue(path)
                .build();

        Packet response = sendPacket(listFilesPacket);
        ServerResponse serverResponse = parse(response, eAlgorithm);

        if (serverResponse.getType() == ResponseType.ERROR) {
            String msg = getMsgOrThrow(serverResponse, new FileServiceException("Error fetching files"));
            throw new FileServiceException("Error fetching files: " + msg);
        }

        String finalPath = path;
        return serverResponse.getFileListList().stream()
                .map(file -> {
                    int length = finalPath.equals("/") ? 1 : finalPath.length() + 1;
                    String filename = file.getFilename().substring(length);
                    return file.toBuilder().setFilename(filename).build();
                })
                .collect(Collectors.toList());
    }
}
