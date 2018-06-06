package com.github.mikee2509.storagecloud.admin.user;

import com.github.mikee2509.storagecloud.admin.client.TcpClientService;
import com.github.mikee2509.storagecloud.admin.client.TcpService;
import com.github.mikee2509.storagecloud.admin.domain.Packet;
import com.github.mikee2509.storagecloud.admin.domain.dto.NewUserDto;
import com.github.mikee2509.storagecloud.admin.domain.exception.ParsingException;
import com.github.mikee2509.storagecloud.admin.domain.exception.UserServiceException;
import com.github.mikee2509.storagecloud.proto.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.mikee2509.storagecloud.admin.domain.ParamId.*;

class TcpUserService extends TcpService implements UserService {
    @Value("${tin.hashAlgorithm}")
    private HashAlgorithm hAlgorithm;
    @Value("${tin.encryptionAlgorithm}")
    private EncryptionAlgorithm eAlgorithm;

    public TcpUserService(TcpClientService tcpClientService) {
        super(tcpClientService);
    }

    @Override
    public String register(NewUserDto newUserDto) {
        Packet registerPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.REGISTER)
                .addParam(USERNAME.toString()).ofValue(newUserDto.getUsername())
                .addParam(PASS.toString()).ofValue(newUserDto.getPassword())
                .addParam(FIRST_NAME.toString()).ofValue(newUserDto.getFirstName())
                .addParam(LAST_NAME.toString()).ofValue(newUserDto.getLastName())
                .build();

        Packet response = sendPacket(registerPacket);
        ServerResponse serverResponse = parse(response, eAlgorithm);

        if (serverResponse.getType() == ResponseType.ERROR) {
            String msg = getMsgOrThrow(serverResponse, new UserServiceException("Error registering user"));
            throw new UserServiceException("Error registering user: " + msg);
        }
        if (serverResponse.getType() == ResponseType.OK) {
            return "OK";
        } else {
            throw new ParsingException("Server returned response of unknown type");
        }
    }

    @Override
    public List<UserDetails> listUsers() {
        Packet listUsersPacket = Packet.builder()
                .hashAlgorithm(hAlgorithm)
                .encryptionAlgorithm(eAlgorithm)
                .command()
                .type(CommandType.LIST_USERS)
                .build();

        Packet response = sendPacket(listUsersPacket);
        ServerResponse serverResponse = parse(response, eAlgorithm);

        if (serverResponse.getType() != ResponseType.USERS) {
            throw new ParsingException("Server returned response of wrong type");
        }
        return serverResponse.getUserListList().stream()
                .filter(user -> user.getRole() != UserRole.ADMIN)
                .collect(Collectors.toList());
    }
}
