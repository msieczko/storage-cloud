package com.github.mikee2509.storagecloud.admin;

import com.github.mikee2509.storagecloud.admin.client.TcpClient;
import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.ParamId;
import com.github.mikee2509.storagecloud.admin.domain.ServerResponseParserException;
import com.github.mikee2509.storagecloud.proto.*;
import com.google.protobuf.ByteString;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Optional;

import static com.github.mikee2509.storagecloud.admin.domain.ParamId.*;
import static com.github.mikee2509.storagecloud.proto.ResponseType.LOGGED;
import static com.github.mikee2509.storagecloud.proto.ResponseType.OK;
import static org.assertj.core.api.Assertions.assertThat;


@Log
@SpringBootTest
@NoArgsConstructor
@ActiveProfiles("mock")
@RunWith(SpringRunner.class)
public class TcpClientTest {
    @Value("${tin.username}")
    private String username;
    @Value("${tin.password}")
    private String password;

    @Autowired
    private TcpClient client;

    private Packet handshake;
    private Packet loginPacket;
    private Packet logoutPacket;
    private static final EncryptionAlgorithm E_ALGORITHM = EncryptionAlgorithm.NOENCRYPTION;
    private static final HashAlgorithm H_ALGORITHM = HashAlgorithm.H_SHA512;

    @Before
    public void setUp() throws Exception {
        handshake = Packet.builder()
                .hashAlgorithm(H_ALGORITHM)
                .handshake()
                .negotiateEncryptionAlgorithm(E_ALGORITHM)
                .build();

        loginPacket = Packet.builder()
                .hashAlgorithm(H_ALGORITHM)
                .command()
                .type(CommandType.LOGIN)
                .addParam(USERNAME.toString()).ofValue(username)
                .addParam(PASSWORD.toString()).ofValue(password)
                .build();

        logoutPacket = Packet.builder()
                .hashAlgorithm(H_ALGORITHM)
                .command()
                .type(CommandType.LOGOUT)
                .build();
    }

    private void sendHandshake() throws ServerResponseParserException, IOException {
        ServerResponse serverResponse = client.sendPacket(handshake).parseServerResponse(E_ALGORITHM);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(OK);
    }

    @Test
    public void testLogin() throws IOException, ServerResponseParserException {
        client.openConnection();

        sendHandshake();

        ServerResponse serverResponse = client.sendPacket(loginPacket).parseServerResponse(E_ALGORITHM);
        log.info(serverResponse.toString());
        ResponseType type = serverResponse.getType();
        assertThat(type == OK || type == LOGGED).isTrue();

        serverResponse = client.sendPacket(loginPacket).parseServerResponse(E_ALGORITHM);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(LOGGED);

        serverResponse = client.sendPacket(logoutPacket).parseServerResponse(E_ALGORITHM);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(OK);

        client.closeConnection();
    }

    @Test
    public void testLoginWithWrongCredentials() throws IOException, ServerResponseParserException {
        Packet wrongCredentialsLoginPacket = Packet.builder()
                .hashAlgorithm(H_ALGORITHM)
                .command()
                .type(CommandType.LOGIN)
                .addParam("username").ofValue(username)
                .addParam("password").ofValue("pa$$w0rd")
                .build();

        client.openConnection();

        sendHandshake();

        ServerResponse serverResponse = client.sendPacket(wrongCredentialsLoginPacket).parseServerResponse(E_ALGORITHM);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(ResponseType.ERROR);

        client.closeConnection();
    }

    @Test
    public void testRelogin() throws IOException, ServerResponseParserException {
        client.openConnection();
        sendHandshake();

        ServerResponse serverResponse = client.sendPacket(loginPacket).parseServerResponse(E_ALGORITHM);
        log.info(serverResponse.toString());
        ResponseType type = serverResponse.getType();
        assertThat(type == OK || type == LOGGED).isTrue();

        Optional<Param> sidOptional = serverResponse.getParamsList().stream()
                .filter(param -> param.getParamId().equals(ParamId.SID.toString()))
                .findFirst();

        assertThat(sidOptional).isPresent();
        //noinspection ConstantConditions
        final ByteString sid = sidOptional.get().getBParamVal();

        client.closeConnection();

        Packet reloginPacket = Packet.builder()
                .hashAlgorithm(H_ALGORITHM)
                .command()
                .type(CommandType.RELOGIN)
                .addParam(SID.toString()).ofValue(sid)
                .addParam(USERNAME.toString()).ofValue(username)
                .build();

        client.openConnection();

        serverResponse = client.sendPacket(reloginPacket).parseServerResponse(E_ALGORITHM);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(LOGGED);

        serverResponse = client.sendPacket(logoutPacket).parseServerResponse(E_ALGORITHM);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(OK);

        client.closeConnection();
    }
}