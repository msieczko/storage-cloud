package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.ServerResponseParserException;
import com.github.mikee2509.storagecloud.admin.domain.exception.ParsingException;
import com.github.mikee2509.storagecloud.admin.domain.exception.SocketConnectionException;
import com.github.mikee2509.storagecloud.proto.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

class TcpUserService implements UserService {
    @Value("${tin.hashAlgorithm}")
    private HashAlgorithm hAlgorithm;
    @Value("${tin.encryptionAlgorithm}")
    private EncryptionAlgorithm eAlgorithm;

    private TcpClientService tcpClientService;

    public TcpUserService(TcpClientService tcpClientService) {
        this.tcpClientService = tcpClientService;
    }

    @Override
    public List<UserDetails> listUsers() {
        Packet listUsersPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.LIST_USERS)
                .build();

        Packet response;
        try {
            response = tcpClientService.sendPacket(listUsersPacket);
        } catch (IOException e) {
            throw new SocketConnectionException("Connection to server failed: " + e.getMessage(), e);
        }

        ServerResponse serverResponse;
        try {
            serverResponse = response.parseServerResponse(eAlgorithm);
        } catch (ServerResponseParserException e) {
            throw new ParsingException("Failed to parse server response: " + e.getMessage(), e);
        }

        if (serverResponse.getType() != ResponseType.USERS) {
            throw new ParsingException("Server returned response of wrong type");
        }
        return serverResponse.getUserListList().stream()
                .filter(user -> user.getRole() != UserRole.ADMIN)
                .collect(Collectors.toList());
    }
}
