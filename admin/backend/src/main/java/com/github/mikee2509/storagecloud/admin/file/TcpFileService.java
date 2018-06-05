package com.github.mikee2509.storagecloud.admin.file;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.ServerResponseParserException;
import com.github.mikee2509.storagecloud.admin.domain.exception.FileServiceException;
import com.github.mikee2509.storagecloud.admin.domain.exception.ParsingException;
import com.github.mikee2509.storagecloud.admin.domain.exception.SocketConnectionException;
import com.github.mikee2509.storagecloud.proto.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.mikee2509.storagecloud.admin.domain.ParamId.*;

class TcpFileService implements FileService {
    @Value("${tin.hashAlgorithm}")
    private HashAlgorithm hAlgorithm;
    @Value("${tin.encryptionAlgorithm}")
    private EncryptionAlgorithm eAlgorithm;

    TcpClientService tcpClientService;

    public TcpFileService(TcpClientService tcpClientService) {
        this.tcpClientService = tcpClientService;
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

        Packet response;
        try {
            response = tcpClientService.sendPacket(listFilesPacket);
        } catch (IOException e) {
            throw new SocketConnectionException("Connection to server failed: " + e.getMessage(), e);
        }

        ServerResponse serverResponse;
        try {
            serverResponse = response.parseServerResponse(eAlgorithm);
        } catch (ServerResponseParserException e) {
            throw new ParsingException("Failed to parse server response: " + e.getMessage(), e);
        }

        if (serverResponse.getType() == ResponseType.ERROR) {
            Param msg = serverResponse.getParamsList().stream()
                    .filter(param -> Objects.equals(param.getParamId(), MSG.toString()))
                    .findFirst()
                    .orElseThrow(() -> new FileServiceException("Error fetching files"));
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
