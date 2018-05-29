package com.github.mikee2509.storagecloud.admin;

import com.github.mikee2509.storagecloud.admin.client.TcpClient;
import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.ServerResponseParserException;
import com.github.mikee2509.storagecloud.proto.*;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@NoArgsConstructor
public class TcpClientTest {
    @Value("${tin.username}")
    private String username;
    @Value("${tin.password}")
    private String password;

    @Autowired
    private TcpClient client;

    @Test
    public void testLogin() throws IOException, ServerResponseParserException {
        Packet loginPacket = Packet.builder()
                .hashAlgorithm(HashAlgorithm.H_SHA512)
                .command()
                .type(CommandType.LOGIN)
                .addParam("username").ofValue(username)
                .addParam("password").ofValue(password)
                .build();

        Packet response = client.sendPacket(loginPacket);
        ServerResponse serverResponse = response.parseServerResponse(EncryptionAlgorithm.NOENCRYPTION);
        ResponseType type = serverResponse.getType();
        assertThat(type == ResponseType.OK || type == ResponseType.LOGGED).isTrue();
        client.stopConnection();
    }

    @Test
    public void testLoginWithWrongCredentials() throws IOException, ServerResponseParserException {
        Packet loginPacket = Packet.builder()
                .hashAlgorithm(HashAlgorithm.H_SHA512)
                .command()
                .type(CommandType.LOGIN)
                .addParam("username").ofValue(username)
                .addParam("password").ofValue("pa$$w0rd")
                .build();

        Packet response = client.sendPacket(loginPacket);
        ServerResponse serverResponse = response.parseServerResponse(EncryptionAlgorithm.NOENCRYPTION);
        assertThat(serverResponse.getType()).isEqualTo(ResponseType.ERROR);
        client.stopConnection();
    }

}