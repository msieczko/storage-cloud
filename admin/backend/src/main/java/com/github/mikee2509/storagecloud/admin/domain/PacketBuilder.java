package com.github.mikee2509.storagecloud.admin.domain;

import com.github.mikee2509.storagecloud.admin.util.EncryptionUtility;
import com.github.mikee2509.storagecloud.admin.util.HashUtility;
import com.github.mikee2509.storagecloud.proto.*;
import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;

public class PacketBuilder {
    private EncryptionAlgorithm encryptionAlgorithm;

    private EncodedMessage.Builder encodedMessageBuilder = EncodedMessage.newBuilder();
    private CommandPacketBuilder commandPacketBuilder;
    private ServerResponsePacketBuilder serverResponsePacketBuilder;
    private HandshakePacketBuilder handshakePacketBuilder;

    public PacketBuilder hashAlgorithm(HashAlgorithm hashAlgorithm) {
        encodedMessageBuilder.setHashAlgorithm(hashAlgorithm);
        return this;
    }

    public PacketBuilder encryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
        return this;
    }

    public CommandPacketBuilder command() {
        encodedMessageBuilder.setType(MessageType.COMMAND);
        if (commandPacketBuilder == null) {
            commandPacketBuilder = new CommandPacketBuilder();
        }
        return commandPacketBuilder;
    }

    public ServerResponsePacketBuilder serverResponse() {
        encodedMessageBuilder.setType(MessageType.SERVER_RESPONSE);
        if (serverResponsePacketBuilder == null) {
            serverResponsePacketBuilder = new ServerResponsePacketBuilder();
        }
        return serverResponsePacketBuilder;
    }

    public HandshakePacketBuilder handshake() {
        encodedMessageBuilder.setType(MessageType.HANDSHAKE);
        if (handshakePacketBuilder == null) {
            handshakePacketBuilder = new HandshakePacketBuilder();
        }
        return handshakePacketBuilder;
    }

    public class CommandPacketBuilder {
        private Command.Builder commandBuilder = Command.newBuilder();

        public CommandPacketBuilder type(CommandType commandType) {
            commandBuilder.setType(commandType);
            return this;
        }

        public ParamBuilder addParam(String paramId) {
            return new ParamBuilder(paramId);
        }

        public CommandPacketBuilder addListItem(String listItem) {
            commandBuilder.addList(listItem);
            return this;
        }

        public CommandPacketBuilder data(ByteString data) {
            commandBuilder.setData(data);
            return this;
        }

        public Packet build() {
            if (commandBuilder.getType() == CommandType.UNRECOGNIZED) {
                throw PacketBuilderException.notProvided("command type");
            }
            return create(commandBuilder.build().toByteArray());
        }

        public class ParamBuilder {
            private Param.Builder paramBuilder = Param.newBuilder();

            private ParamBuilder(String paramId) {
                paramBuilder.setParamId(paramId);
            }

            public CommandPacketBuilder ofValue(String value) {
                paramBuilder.setSParamVal(value);
                return buildAndGetParent();
            }

            public CommandPacketBuilder ofValue(long value) {
                paramBuilder.setIParamVal(value);
                return buildAndGetParent();
            }

            public CommandPacketBuilder ofValue(ByteString value) {
                paramBuilder.setBParamVal(value);
                return buildAndGetParent();
            }

            private CommandPacketBuilder buildAndGetParent() {
                commandBuilder.addParams(paramBuilder.build());
                return CommandPacketBuilder.this;
            }
        }
    }

    public class ServerResponsePacketBuilder {
        private ServerResponse.Builder responseBuilder = ServerResponse.newBuilder();

        public ServerResponsePacketBuilder type(ResponseType responseType) {
            responseBuilder.setType(responseType);
            return this;
        }

        public ParamBuilder addParam(String paramId) {
            return new ParamBuilder(paramId);
        }

        public ServerResponsePacketBuilder addListItem(String listItem) {
            responseBuilder.addList(listItem);
            return this;
        }

        public FileBuilder addFile(String name) {
            return new FileBuilder(name);
        }

        public ServerResponsePacketBuilder data(ByteString data) {
            responseBuilder.setData(data);
            return this;
        }

        public Packet build() {
            if (responseBuilder.getType() == ResponseType.UNRECOGNIZED) {
                throw PacketBuilderException.notProvided("server response type");
            }
            return create(responseBuilder.build().toByteArray());
        }

        public class ParamBuilder {
            private Param.Builder paramBuilder = Param.newBuilder();

            private ParamBuilder(String paramId) {
                paramBuilder.setParamId(paramId);
            }

            public ServerResponsePacketBuilder ofValue(String value) {
                paramBuilder.setSParamVal(value);
                return buildAndGetParent();
            }

            public ServerResponsePacketBuilder ofValue(long value) {
                paramBuilder.setIParamVal(value);
                return buildAndGetParent();
            }

            public ServerResponsePacketBuilder ofValue(ByteString value) {
                paramBuilder.setBParamVal(value);
                return buildAndGetParent();
            }

            private ServerResponsePacketBuilder buildAndGetParent() {
                responseBuilder.addParams(paramBuilder.build());
                return ServerResponsePacketBuilder.this;
            }
        }

        public class FileBuilder {
            private File.Builder fileBuilder = File.newBuilder();

            private FileBuilder(String name) {
                fileBuilder.setFilename(name);
            }

            public FileBuilder ofSize(long size) {
                fileBuilder.setSize(size);
                return this;
            }

            public ParamBuilder withMetadata(String paramId) {
                return new ParamBuilder(paramId);
            }

            public ServerResponsePacketBuilder and() {
                return ServerResponsePacketBuilder.this;
            }

            public class ParamBuilder {
                private Param.Builder paramBuilder = Param.newBuilder();

                private ParamBuilder(String paramId) {
                    paramBuilder.setParamId(paramId);
                }

                public FileBuilder ofValue(String value) {
                    paramBuilder.setSParamVal(value);
                    return buildAndGetParent();
                }

                public FileBuilder ofValue(long value) {
                    paramBuilder.setIParamVal(value);
                    return buildAndGetParent();
                }

                public FileBuilder ofValue(ByteString value) {
                    paramBuilder.setBParamVal(value);
                    return buildAndGetParent();
                }

                private FileBuilder buildAndGetParent() {
                    fileBuilder.addMetadata(paramBuilder.build());
                    return FileBuilder.this;
                }
            }
        }
    }

    public class HandshakePacketBuilder {
        Handshake.Builder handshakeBuilder = Handshake.newBuilder();

        public HandshakePacketBuilder encryptionAlgorithm(EncryptionAlgorithm algorithm) {
            handshakeBuilder.setEncryptionAlgorithm(algorithm);
            return this;
        }

        public Packet build() {
            if (handshakeBuilder.getEncryptionAlgorithm() == EncryptionAlgorithm.UNRECOGNIZED) {
                throw PacketBuilderException.notProvided("encryption algorithm");
            }
            return create(handshakeBuilder.build().toByteArray());
        }
    }

    private Packet create(byte[] bytes) {
        if (encryptionAlgorithm == null) {
            encryptionAlgorithm = EncryptionAlgorithm.NOENCRYPTION;
        }
        if (encodedMessageBuilder.getHashAlgorithm() == HashAlgorithm.UNRECOGNIZED) {
            throw PacketBuilderException.notProvided("hash algorithm");
        }
        byte[] hash = HashUtility.digest(bytes, encodedMessageBuilder.getHashAlgorithm());
        encodedMessageBuilder.setHash(ByteString.copyFrom(hash));

        byte[] encryptedData = EncryptionUtility.encrypt(bytes, encryptionAlgorithm);

        encodedMessageBuilder.setDataSize(encryptedData.length);
        encodedMessageBuilder.setData(ByteString.copyFrom(encryptedData));
        EncodedMessage encodedMessage = encodedMessageBuilder.build();

        byte[] packetPayload = encodedMessage.toByteArray();
        byte[] packetSize = ByteBuffer.allocate(4).putInt(packetPayload.length + 4).array();

        byte[] packetData = new byte[packetSize.length + packetPayload.length];
        System.arraycopy(packetSize, 0, packetData, 0, packetSize.length);
        System.arraycopy(packetPayload, 0, packetData, packetSize.length, packetPayload.length);

        return new Packet(packetData);
    }
}
