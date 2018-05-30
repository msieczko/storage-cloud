using System;
using System.CodeDom;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace StorageCloud.Desktop.Protocol
{
    public class PacketManager
    {
        private byte[] buffer = new byte[4096];
        private NetworkStream stream;
        private TcpClient client;
        private PacketCoder packetCoder;
        private string serverIp;
        private Int32 port;

        public PacketManager(string serverIp, Int32 port)
        {
            this.serverIp = serverIp;
            this.port = port;
            packetCoder = new PacketCoder(EncryptionAlgorithm.Noencryption);
        }

        public void Connect()
        {
            client = new TcpClient(serverIp, port);
            stream = client.GetStream();
            log("Connected to the server");
        }

        public void CloseConnection()
        {
            // Close everything.
            stream.Close();
            client.Close();
        }

        private void Send(byte[] data)
        {
            log("Sending a message of size " + data.Length + " bytes");
            stream.Write(data, 0, data.Length);
        }

        private void SendAndRead(byte[] data)
        {
            log("Sending a message");
            stream.Write(data, 0, data.Length);

            log("Waiting for a message from the server");
            stream.Read(buffer, 0, buffer.Length);
            log("Got a message from the server");
        }

        private ServerResponse SendAndGetServerResponse(byte[] data)
        {
            SendAndRead(data);
            PacketCoder.Packet packet = packetCoder.DecodeMessage(buffer);
            Console.WriteLine("Got server response: " + ((ServerResponse)packet.Message).Type);
            return (ServerResponse) packet.Message;
        }

        public ServerResponse Handshake()
        {
            byte[] handshakePacket = packetCoder.CreateHandshakePacket();
            ServerResponse response = SendAndGetServerResponse(handshakePacket);
            return response;
        }

        public ServerResponse Login(string username, string password)
        {
            byte[] loginPacket = packetCoder.CreateLoginPacket(username, password);
            ServerResponse response = SendAndGetServerResponse(loginPacket);
            return response;
        }

        public ServerResponse Relog(byte[] sid, string username)
        {
            Console.WriteLine("Packet manager: relog!");
            byte[] loginPacket = packetCoder.CreateLoginPacket(sid, username);
            ServerResponse response = SendAndGetServerResponse(loginPacket);
            return response;
        }

        public ServerResponse Logout()
        {
            byte[] logoutPacket = packetCoder.CreateLogoutPacket();
            ServerResponse response = SendAndGetServerResponse(logoutPacket);
            return response;
        }

        private void log(string message)
        {
            Console.WriteLine(message);
        }
    }
}
