package com.github.mikee2509.storagecloud.admin.security;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.ServerResponseParserException;
import com.github.mikee2509.storagecloud.admin.domain.exception.LoginException;
import com.github.mikee2509.storagecloud.admin.domain.security.SessionDetails;
import com.github.mikee2509.storagecloud.proto.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

import static com.github.mikee2509.storagecloud.admin.domain.ParamId.*;

@Component
public class ServerAuthenticationProvider implements AuthenticationProvider {
    @Value("${tin.hashAlgorithm}")
    private HashAlgorithm hAlgorithm;
    @Value("${tin.encryptionAlgorithm}")
    private EncryptionAlgorithm eAlgorithm;

    private TcpClientService tcpClientService;
    private static final LoginException SERVER_ERROR_EXCEPTION =
            new LoginException("Failed to authenticate: server error");


    public ServerAuthenticationProvider(TcpClientService tcpClientService) {
        this.tcpClientService = tcpClientService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        ByteString sid = login(username, password);
        UserDetails userDetails = getUserDetails(username);

        SessionDetails authDetails = SessionDetails.create(userDetails, sid);
        return new UsernamePasswordAuthenticationToken(authDetails, null, authDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private ByteString login(String username, String password) {
        Packet loginPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .command()
                .type(CommandType.LOGIN)
                .addParam(USERNAME.toString()).ofValue(username)
                .addParam(PASSWORD.toString()).ofValue(password)
                .build();

        Packet response = openConnectionAndSend(loginPacket);
        ServerResponse serverResponse = parse(response);
        return extractSid(serverResponse);
    }

    public SessionDetails relogin(SessionDetails sessionDetails) {
        Packet reloginPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .command()
                .type(CommandType.RELOGIN)
                .addParam(SID.toString()).ofValue(sessionDetails.getSid())
                .addParam(USERNAME.toString()).ofValue(sessionDetails.getUsername())
                .build();

        Packet response = openConnectionAndSend(reloginPacket);
        ServerResponse serverResponse = parse(response);
        ByteString sid = extractSid(serverResponse);
        UserDetails userDetails = getUserDetails(sessionDetails.getUsername());

        return SessionDetails.create(userDetails, sid);
    }

    private Packet openConnectionAndSend(Packet packet) {
        try {
            if (!tcpClientService.isConnected()) {
                tcpClientService.openConnection();
            }
            return tcpClientService.sendPacket(packet);
        } catch (IOException e) {
            throw new LoginException("Connection to server failed: " + e.getMessage(), e);
        }
    }

    private ServerResponse parse(Packet responsePacket) {
        try {
            return  responsePacket.parseServerResponse(eAlgorithm);
        } catch (ServerResponseParserException e) {
            throw new LoginException("Failed to parse server response: " + e.getMessage(), e);
        }
    }

    private ByteString extractSid(ServerResponse serverResponse) {
        switch (serverResponse.getType()) {
            case LOGGED:
                Param sid = serverResponse.getParamsList().stream()
                        .filter(param -> Objects.equals(param.getParamId(), SID.toString()))
                        .findFirst()
                        .orElseThrow(() -> SERVER_ERROR_EXCEPTION);
                if (sid.getBParamVal() == null) {
                    throw SERVER_ERROR_EXCEPTION;
                }
                return sid.getBParamVal();
            case ERROR:
                Param msg = serverResponse.getParamsList().stream()
                        .filter(param -> Objects.equals(param.getParamId(), MSG.toString()))
                        .findFirst()
                        .orElseThrow(() -> new LoginException("Failed to authenticate"));
                throw new LoginException("Failed to authenticate: " + msg.getSParamVal());
            default:
                throw SERVER_ERROR_EXCEPTION;

        }
    }

    private UserDetails getUserDetails(String username) {
        Packet listUsersPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .command()
                .type(CommandType.LIST_USERS)
                .build();
        Packet response = openConnectionAndSend(listUsersPacket);
        ServerResponse serverResponse = parse(response);

        switch (serverResponse.getType()) {
            case USERS:
                return serverResponse.getUserListList().stream()
                        .filter(user -> Objects.equals(user.getUsername(), username))
                        .findFirst()
                        .orElseThrow(() -> SERVER_ERROR_EXCEPTION);
            case ERROR:
                Param param = serverResponse.getParamsList().stream()
                        .filter(p -> Objects.equals(p.getParamId(), MSG.toString()))
                        .findFirst()
                        .orElseThrow(() -> SERVER_ERROR_EXCEPTION);
                if (param.getSParamVal() == null) {
                    throw SERVER_ERROR_EXCEPTION;
                }
                throw new LoginException("Authentication failed: " + param.getSParamVal().toLowerCase());
            default:
                throw SERVER_ERROR_EXCEPTION;
        }
    }
}
