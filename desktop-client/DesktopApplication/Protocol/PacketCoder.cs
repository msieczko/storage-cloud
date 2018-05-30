using System;
using System.Linq;
using System.Security.Cryptography;
using Google.Protobuf;

namespace StorageCloud.Desktop.Protocol
{
    class PacketCoder
    {
        public PacketCoder(EncryptionAlgorithm encryptionAlgorithm)
        {
            this.encryptionAlgorithm = encryptionAlgorithm;
        }

        // a struct holding a packet from an encoded message
        // as well as its type
        public struct Packet
        {
            public MessageType Type;
            public IMessage Message;
        }

        // prepare a packet to be sent
        public byte[] EncodeMessage(Packet packet)
        {
            EncodedMessage encodedMessage = CreateEncodedMessageFromPacket(packet, 
                encryptionAlgorithm, HashAlgorithm.HSha512);
            byte[] payload = encodedMessage.ToByteArray();
            byte[] packetSize = convertIntToByteArray(payload.Length + 4);
            byte[] output = new byte[payload.Length + packetSize.Length];
            Array.Copy(packetSize, output, packetSize.Length);
            Array.Copy(payload, 0, output, packetSize.Length, payload.Length);

            return output;
        }

        // parse a received packet
        public Packet DecodeMessage(byte[] data)
        {
            Console.WriteLine("Decoding message...");
            byte[] packetSizeInBytes = new byte[4];
            Array.Copy(data, 0, packetSizeInBytes, 0, 4);
            Int32 packetSize = convertByteArrayToInt(packetSizeInBytes);
            Console.WriteLine("Decoding message of length: " + packetSize);

            // parse the encoded message from bytes
            EncodedMessage encodedMessage = EncodedMessage.Parser.ParseFrom(data, 4, packetSize - 4);
            // compare hashes
            byte[] incomingHash = encodedMessage.Hash.ToByteArray(); // convert ByteString to byte[]
            byte[] currentHash = CalculateHash(encodedMessage.Data.ToByteArray(), encodedMessage.HashAlgorithm);
//            if (CompareHash(incomingHash, currentHash))
//            {
//                throw new InvalidOperationException("The hash in the incoming packet " +
//                                                    "does not match the computed hash");
//            }
            // return the packet from the encoded message
            return CreatePacketFromEncodedMessage(encodedMessage, encryptionAlgorithm);
        }

        public byte[] CreateHandshakePacket()
        {
            Handshake handshake = new Handshake()
            {
                EncryptionAlgorithm = encryptionAlgorithm
            };
            Packet packet = new Packet()
            {
                Message = handshake,
                Type = MessageType.Handshake
            };

            return EncodeMessage(packet);
        }

        public byte[] CreateLoginPacket(string username, string password)
        {
            Command command = new Command()
            {
                Type = CommandType.Login,
                Params =
                {
                    new Param()
                    {
                        ParamId = "username",
                        SParamVal = username
                    },
                    new Param()
                    {
                        ParamId = "password",
                        SParamVal = password
                    }
                }
            };
            Packet packet = new Packet()
            {
                Message = command,
                Type = MessageType.Command
            };

            return EncodeMessage(packet);
        }

        public byte[] CreateLoginPacket(byte[] sid, string username)
        {
            Command command = new Command()
            {
                Type = CommandType.Relogin,
                Params =
                {
                    new Param()
                    {
                        ParamId = "username",
                        SParamVal = username
                    },
                    new Param()
                    {
                        ParamId = "sid",
                        BParamVal = ByteString.CopyFrom(sid)
                    }
                }
            };
            Packet packet = new Packet()
            {
                Message = command,
                Type = MessageType.Command
            };

            return EncodeMessage(packet);
        }

        public byte[] CreateLogoutPacket()
        {
            Command command = new Command()
            {
                Type = CommandType.Logout
            };
            Packet packet = new Packet()
            {
                Message = command,
                Type = MessageType.Command
            };

            return EncodeMessage(packet);
        }

        private static byte[] convertIntToByteArray(int value)
        {
            byte[] bytes = BitConverter.GetBytes(value);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }

            return bytes;
        }

        private static int convertByteArrayToInt(byte[] data)
        {
            if (data.Length != 4)
            {
                throw new ArgumentException("Cannot parse int from array of length not equal 4.");
            }

            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(data);
            }
            int i = BitConverter.ToInt32(data, 0);

            return i;
        }

        private static Packet CreatePacketFromEncodedMessage(EncodedMessage encodedMessage, 
            EncryptionAlgorithm encryptionAlgorithm)
        {
            IMessage innerMesage = 
                GetMessageFromEncodedMessage(encodedMessage, encryptionAlgorithm);
            Packet packet = new Packet()
            {
                Type = encodedMessage.Type,
                Message = innerMesage
            };
            return packet;
        }

        private static IMessage GetMessageFromEncodedMessage(EncodedMessage encodedMessage,
            EncryptionAlgorithm encryptionAlgorithm)
        {
            // decrypt the data from the encoded message
            byte[] data = encodedMessage.Data.ToByteArray(); // converting Google.Protobuf.ByteString to byte[]
            DecryptData(data, encryptionAlgorithm);

            // parse a proper message from the bytes data
            MessageType type = encodedMessage.Type;
            switch (type)
            {
                case MessageType.ServerResponse:
                    return ServerResponse.Parser.ParseFrom(data);
                case MessageType.Command:
                    return Command.Parser.ParseFrom(data);
                case MessageType.Handshake:
                    return Handshake.Parser.ParseFrom(data);
                case MessageType.Null3:
                    throw CreateNotImplementedException("Null3 message type");
                default:
                    throw new ArgumentOutOfRangeException(nameof(type), type, null);
            }
        }

        // true if hashes are equal
        private static bool CompareHash(byte[] hash1, byte[] hash2)
        {
            return Enumerable.SequenceEqual(hash1, hash2);
        }

        private static void EncryptData(byte[] data, EncryptionAlgorithm encryptionAlgorithm)
        {
            switch (encryptionAlgorithm)
            {
                case EncryptionAlgorithm.Null4:
                    throw CreateNotImplementedException("Null4 encryption algorithm");
                case EncryptionAlgorithm.Noencryption:
                    break;
                case EncryptionAlgorithm.Caesar:
                    for (int i = 0; i < data.Length; ++i)
                    {
                        data[i] = data[i] == 255 ? (byte)0 : (byte)(data[i] + 1);
                    }
                    break;
                default:
                    throw new ArgumentOutOfRangeException(nameof(encryptionAlgorithm), encryptionAlgorithm, null);
            }
        }

        private static void DecryptData(byte[] data, EncryptionAlgorithm encryptionAlgorithm)
        {
            switch (encryptionAlgorithm)
            {
                case EncryptionAlgorithm.Null4:
                    throw new NotImplementedException("Null4 encryption algorithm has not been implemented yet");
                case EncryptionAlgorithm.Noencryption:
                    break;
                case EncryptionAlgorithm.Caesar:
                    for (var i = 0; i < data.Length; ++i)
                    {
                        data[i] = data[i] == 0 ? (byte)255 : (byte)(data[i] - 1);
                    }
                    break;
                default:
                    throw new ArgumentOutOfRangeException(nameof(encryptionAlgorithm), encryptionAlgorithm, null);
            }
        }

        private static EncodedMessage CreateEncodedMessageFromPacket(Packet packet, 
            EncryptionAlgorithm encryptionAlgorithm, HashAlgorithm hashAlgorithm)
        {
            // serialize and encrypt packet message
            byte[] data = packet.Message.ToByteArray();
            EncryptData(data, encryptionAlgorithm);
            // create Encoded Message
            EncodedMessage encodedMessage = new EncodedMessage()
            {
                DataSize = (ulong)packet.Message.CalculateSize(),
                HashAlgorithm = hashAlgorithm,
                Hash = ByteString.CopyFrom(CalculateHash(data, hashAlgorithm)),
                Type = packet.Type,
                Data = ByteString.CopyFrom(data)
            };

            return encodedMessage;
        }

        private static byte[] CalculateHash(byte[] data, HashAlgorithm algorithm)
        {
            switch (algorithm)
            {
                case HashAlgorithm.Null2:
                    throw CreateNotImplementedException("Null2");
                case HashAlgorithm.HNohash:
                    throw CreateNotImplementedException("Nohash");
                case HashAlgorithm.HSha256:
                    return SHA256.Create().ComputeHash(data);
                case HashAlgorithm.HSha512:
                    return SHA512.Create().ComputeHash(data);
                case HashAlgorithm.HSha1:
                    return SHA1.Create().ComputeHash(data);
                case HashAlgorithm.HMd5:
                    return MD5.Create().ComputeHash(data);
                default:
                    throw new ArgumentOutOfRangeException(nameof(algorithm), algorithm, null);
            }
        }

        private static NotImplementedException CreateNotImplementedException(string what)
        {
            return new NotImplementedException(what + " has not been implemented yet");
        }

        private EncryptionAlgorithm encryptionAlgorithm;
    }
}
