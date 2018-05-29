package com.github.mikee2509.storagecloud.admin.client;

import com.github.mikee2509.storagecloud.admin.domain.Packet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

@Service
public class TcpClient {
    private static final int BUFFER_SIZE = 1024;

    @Value("${tin.server.address}")
    private String serverAddress;
    @Value("${tin.server.port}")
    private int serverPort;

    private Socket clientSocket;
    private OutputStream socketOutputStream;
    private InputStream socketInputStream;

    public Packet sendPacket(Packet packet) throws IOException {
        if (clientSocket == null || clientSocket.isClosed()) {
            clientSocket = new Socket(serverAddress, serverPort);
        }
        socketOutputStream = clientSocket.getOutputStream();
        socketOutputStream.write(packet.getData());

        socketInputStream = clientSocket.getInputStream();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] responseSize = new byte[4];
        if (socketInputStream.read(responseSize) != 4) {
            throw new IOException("Failed to receive server response size");
        }
        buffer.write(responseSize);

        int payloadSize = ByteBuffer.wrap(responseSize).getInt() - 4;
        byte[] responsePayload = new byte[payloadSize];
        if (socketInputStream.read(responsePayload) != payloadSize) {
            throw new IOException("Failed to receive server response payload");
        }
        buffer.write(responsePayload);
        buffer.flush();
        return new Packet(buffer.toByteArray());
    }

    public void stopConnection() throws IOException {
        socketInputStream.close();
        socketOutputStream.close();
        clientSocket.close();
    }
}