using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using Google.Protobuf;
using StorageCloud.Desktop.Protobuf;
using HashAlgorithm = StorageCloud.Desktop.Protobuf.HashAlgorithm;

namespace StorageCloud.Desktop
{
    class Protocol
    {
        struct Packet
        {
            public MessageType Type;
            public IMessage Message;
        }

        // prepare a packet to be sent
        static byte[] EncodeMessage(Packet packet)
        {
            EncodedMessage encodedMessage = CreateEncodedMessageFromPacket(packet);
            byte[] serializedMessage = encodedMessage.ToByteArray();
            return serializedMessage;
        }

        // parse a received packet
        static Packet DecodeMessage(byte[] data)
        {
            // parse the encoded message from the bytes
            EncodedMessage encodedMessage = EncodedMessage.Parser.ParseFrom(data);
            // chech hashes
            byte[] incomingHash = encodedMessage.Hash.ToByteArray();
            byte[] currentHash = CalculateHash(encodedMessage.Data.ToByteArray(), HashAlgorithm.HSha512);
            if (CompareHash(incomingHash, currentHash))
            {
                throw new InvalidOperationException("The hash in the incoming packet does not match with the computed hash");
            }
            // return the packet from the encoded message
            return CreatePacketFromEncodedMessage(encodedMessage);
        }

        private static Packet CreatePacketFromEncodedMessage(EncodedMessage encodedMessage)
        {
            IMessage innerMesage = 
                GetMessageFromEncodedMessage(encodedMessage.Type, encodedMessage.Data.ToByteArray());
            Packet packet = new Packet()
            {
                Type = encodedMessage.Type,
                Message = innerMesage
            };
            return packet;
        }

        private static IMessage GetMessageFromEncodedMessage(MessageType type, byte[] data)
        {
            DecryptData(data, EncryptionAlgorithm.Caesar);
            switch (type)
            {
                case MessageType.ServerResponse:
                    return ServerResponse.Parser.ParseFrom(data);
                case MessageType.Command:
                    return Command.Parser.ParseFrom(data);
                case MessageType.Handshake:
                    return Handshake.Parser.ParseFrom(data);
                case MessageType.Null3:
                    throw new Exception();
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
                    throw new NotImplementedException("Null4 encryption algorithm has not been implemented yet");
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

        private static EncodedMessage CreateEncodedMessageFromPacket(Packet packet)
        {
            // serialize and encrypt packet message
            byte[] data = packet.Message.ToByteArray();
            EncryptData(data, EncryptionAlgorithm.Caesar);
            // create Encoded Message
            EncodedMessage encodedMessage = new EncodedMessage()
            {
                DataSize = (ulong)packet.Message.CalculateSize(),
                HashAlgorithm = HashAlgorithm.HSha512,
                Hash = ByteString.CopyFrom(CalculateHash(data, HashAlgorithm.HSha512)),
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
                    byte[] bytes = {new byte()};
                    return bytes;
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

        private static NotImplementedException CreateNotImplementedException(string prefix)
        {
            return new NotImplementedException(prefix + " has not been implemented yet");
        }

        static void Main(string[] args)
        {

        }
    }
}
