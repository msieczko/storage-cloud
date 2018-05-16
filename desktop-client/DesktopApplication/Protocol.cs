using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Google.Protobuf;
using StorageCloud.Desktop.Protobuf;

namespace StorageCloud.Desktop
{
    class Protocol
    {
        struct Packet
        {
            public MessageType type;
            public IMessage message;
        }

        // parse received packet
        static void DecodeMessage(byte[] data)
        {
            EncodedMessage message = EncodedMessage.Parser.ParseFrom(data);
            Packet packet = GetPacketFromEncodedMessage(message);
        }

        // prepare packet to be sent
        static void EncodeMessage(Packet packet)
        {

        }

        private static Packet GetPacketFromEncodedMessage(EncodedMessage encodedMessage)
        {
            IMessage innerMesage = 
                GetMessageFromEncodedMessage(encodedMessage.Type, encodedMessage.Data.ToByteArray());
            Packet packet = new Packet()
            {
                type = encodedMessage.Type,
                message = innerMesage
            };
            return packet;
        }

        private static IMessage GetMessageFromEncodedMessage(MessageType type, byte[] data)
        {
            DecipherData(data);
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

        private static void CheckHash()
        {

        }

        private static void DecipherData(byte[] data)
        {
            for (var i = 0; i < data.Length; ++i)
            {
                if (data[i] == 0)
                {
                    data[i] = 255;
                }
                else data[i] -= 1;
            }
        }

        static void Main(string[] args)
        {

        }
    }
}
