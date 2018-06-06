package com.github.mikee2509.storagecloud.admin.client;

import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.ServerResponseParserException;
import com.github.mikee2509.storagecloud.admin.domain.exception.ParsingException;
import com.github.mikee2509.storagecloud.admin.domain.exception.SocketConnectionException;
import com.github.mikee2509.storagecloud.proto.EncryptionAlgorithm;
import com.github.mikee2509.storagecloud.proto.Param;
import com.github.mikee2509.storagecloud.proto.ServerResponse;

import java.io.IOException;
import java.util.Objects;

import static com.github.mikee2509.storagecloud.admin.domain.ParamId.MSG;

public abstract class TcpService {
    protected TcpClientService tcpClientService;

    public TcpService(TcpClientService tcpClientService) {
        this.tcpClientService = tcpClientService;
    }

    public Packet sendPacket(Packet packet) {
        try {
            return tcpClientService.sendPacket(packet);
        } catch (IOException e) {
            throw new SocketConnectionException("Connection to server failed: " + e.getMessage(), e);
        }
    }

    public ServerResponse parse(Packet response, EncryptionAlgorithm eAlgorithm) {
        try {
            return response.parseServerResponse(eAlgorithm);
        } catch (ServerResponseParserException e) {
            throw new ParsingException("Failed to parse server response: " + e.getMessage(), e);
        }
    }

    public String getMsgOrThrow(ServerResponse serverResponse, RuntimeException e) {
        Param param = serverResponse.getParamsList().stream()
                .filter(p -> Objects.equals(p.getParamId(), MSG.toString()))
                .findFirst()
                .orElseThrow(() -> e);
        if (param.getSParamVal() == null) {
            throw e;
        }
        return param.getSParamVal();
    }
}
