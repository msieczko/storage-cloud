package com.github.mikee2509.storagecloud.admin.client;

import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.exception.TcpClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

@Component
public class TcpClientService {
    private static final int SO_TIMEOUT = 5000;

    @Value("${tin.server.address}")
    private String serverAddress;
    @Value("${tin.server.port}")
    private int serverPort;

    private Socket clientSocket;
    private OutputStream socketOutputStream;
    private InputStream socketInputStream;

    public boolean isConnected() {
        return clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }

    public void openConnection() throws IOException {
        if (!isConnected()) {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(serverAddress, serverPort), SO_TIMEOUT);

        }
        socketOutputStream = clientSocket.getOutputStream();
        socketInputStream = clientSocket.getInputStream();
    }

    public Packet sendPacket(Packet packet) throws IOException {
        if (!isConnected()) {
            throw new TcpClientException("No open connection");
        }
        socketOutputStream.write(packet.getData());
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] responseSize = new byte[4];
        if (socketInputStream.read(responseSize) != 4) {
            throw new TcpClientException("Failed to receive response size");
        }
        buffer.write(responseSize);

        int payloadSize = ByteBuffer.wrap(responseSize).getInt() - 4;
        byte[] responsePayload = new byte[payloadSize];
        if (socketInputStream.read(responsePayload) != payloadSize) {
            throw new TcpClientException("Failed to receive response payload");
        }
        buffer.write(responsePayload);
        buffer.flush();
        return new Packet(buffer.toByteArray());
    }

    @PreDestroy
    public void closeConnection() throws IOException {
        if (socketInputStream != null) socketInputStream.close();
        if (socketOutputStream != null) socketOutputStream.close();
        if (clientSocket != null) clientSocket.close();
    }
}