package com.github.mikee2509.storagecloud.admin;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
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
public class TcpClientServiceTest {
    @Value("${tin.username}")
    private String username;
    @Value("${tin.password}")
    private String password;
    @Value("${tin.hashAlgorithm}")
    private HashAlgorithm hAlgorithm;
    @Value("${tin.encryptionAlgorithm}")
    private EncryptionAlgorithm eAlgorithm;


    @Autowired
    private TcpClientService client;

    private Packet handshake;
    private Packet loginPacket;
    private Packet logoutPacket;

    @Before
    public void setUp() throws Exception {
        handshake = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .handshake()
                .negotiateEncryptionAlgorithm(eAlgorithm)
                .build();

        loginPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.LOGIN)
                .addParam(USERNAME.toString()).ofValue(username)
                .addParam(PASSWORD.toString()).ofValue(password)
                .build();

        logoutPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.LOGOUT)
                .build();
    }

    private void sendHandshake() throws ServerResponseParserException, IOException {
        ServerResponse serverResponse = client.sendPacket(handshake).parseServerResponse(eAlgorithm);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(OK);
    }

    private ServerResponse sendLogin() throws ServerResponseParserException, IOException {
        ServerResponse serverResponse = client.sendPacket(loginPacket).parseServerResponse(eAlgorithm);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType() == LOGGED).isTrue();
        return serverResponse;
    }

    private void sendLogout() throws ServerResponseParserException, IOException {
        ServerResponse serverResponse = client.sendPacket(logoutPacket).parseServerResponse(eAlgorithm);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(OK);
    }

    @Test
    public void testLogin() throws IOException, ServerResponseParserException {
        client.openConnection();

        sendHandshake();
        sendLogin();
        sendLogout();

        client.closeConnection();
    }

    @Test
    public void testLoginWithWrongCredentials() throws IOException, ServerResponseParserException {
        Packet wrongCredentialsLoginPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.LOGIN)
                .addParam("username").ofValue(username)
                .addParam("password").ofValue("pa$$w0rd")
                .build();

        client.openConnection();

        sendHandshake();

        ServerResponse serverResponse = client.sendPacket(wrongCredentialsLoginPacket).parseServerResponse(eAlgorithm);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(ResponseType.ERROR);

        client.closeConnection();
    }

    @Test
    public void testRelogin() throws IOException, ServerResponseParserException {
        client.openConnection();
        sendHandshake();
        ServerResponse serverResponse = sendLogin();

        Optional<Param> sidOptional = serverResponse.getParamsList().stream()
                .filter(param -> param.getParamId().equals(ParamId.SID.toString()))
                .findFirst();

        assertThat(sidOptional).isPresent();
        //noinspection ConstantConditions
        final ByteString sid = sidOptional.get().getBParamVal();

        client.closeConnection();

        Packet reloginPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.RELOGIN)
                .addParam(SID.toString()).ofValue(sid)
                .addParam(USERNAME.toString()).ofValue(username)
                .build();

        client.openConnection();

        serverResponse = client.sendPacket(reloginPacket).parseServerResponse(eAlgorithm);
        log.info(serverResponse.toString());
        assertThat(serverResponse.getType()).isEqualTo(LOGGED);

        sendLogout();
        client.closeConnection();
    }

    @Test
    public void testListFiles() throws IOException, ServerResponseParserException {
        client.openConnection();
        sendHandshake();
        sendLogin();

        Packet listFilesPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.LIST_FILES)
                .addParam(PATH.toString()).ofValue("/")
                .build();

        ServerResponse serverResponse = client.sendPacket(listFilesPacket).parseServerResponse(eAlgorithm);
        log.info(serverResponse.toString());

        sendLogout();
        client.closeConnection();
    }

    @Test
    public void testListUserFiles() throws IOException, ServerResponseParserException {
        client.openConnection();
        sendHandshake();
        sendLogin();

        Packet listUserFilesPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.LIST_USER_FILES)
                .addParam(USERNAME.toString()).ofValue("miloszXD")
                .addParam(PATH.toString()).ofValue("/test_dirXDD")
                .build();

        ServerResponse serverResponse = client.sendPacket(listUserFilesPacket).parseServerResponse(eAlgorithm);
        log.info(serverResponse.toString());

        sendLogout();
        client.closeConnection();
    }
}