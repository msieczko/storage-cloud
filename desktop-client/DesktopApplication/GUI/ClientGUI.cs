using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using StorageCloud.Desktop.Protobuf;

namespace StorageCloud.Desktop.Protocol
{
    public partial class ClientGui : Form
    {
        public ClientGui()
        {
            InitializeComponent();
        }

        private void ClientGui_Load(object sender, EventArgs e)
        {
            buffer = new byte[10000];
        }

        void Connect(string serverIp, string message)
        {
            try
            {
                // Create a TcpClient.
                // The client requires a TcpServer that is connected
                // to the same address specified by the server and port
                // combination.
                Int32 port = 52137;
                client = new TcpClient(serverIp, port);
                stream = client.GetStream();
                log("Connected to the server");
            }
            catch (SocketException e)
            {
                string output = "SocketException: " + e.ToString();
                Console.WriteLine(output);
            }
        }

        void CloseConnection()
        {
            // Close everything.
            stream.Close();
            client.Close();
        }

        void Send(byte[] data)
        {
            log("Sending a message of size " + data.Length + " bytes");
            stream.Write(data, 0, data.Length);
        }

        void SendAndGetResponse(byte[] data)
        {
            log("Sending a message");
            stream.Write(data, 0, data.Length);

            log("Waiting for a message from the server");
            stream.Read(buffer, 0, buffer.Length);
            log("Got a message from the server");
        }

        static void Main()
        {
            Application.Run(new ClientGui());
        }

        private void button1_Click(object sender, EventArgs e)
        {
            // In this code example, use a hard-coded
            // IP address and message.
            string serverIP = "mily20001.ddns.net";
            string message = "52137";
            Connect(serverIP, message);
        }

        private void log(string message)
        {
            Console.WriteLine(message);
        }

        private NetworkStream stream;
        private TcpClient client;

        private void ClientGui_FormClosing(object sender, FormClosingEventArgs e)
        {
            CloseConnection();
        }

        private void handshakeButton_Click(object sender, EventArgs e)
        {
            byte[] handshakePacket = PacketCoder.CreateHandshakePacket(encryptionAlgorithm);
            Send(handshakePacket);
        }

        private void loginButton_Click(object sender, EventArgs e)
        {
            byte[] loginPacket = PacketCoder.CreateLoginPacket(encryptionAlgorithm);
            Send(loginPacket);
//            SendAndGetResponse(loginPacket);
//            PacketCoder.Packet packet = PacketCoder.DecodeMessage(buffer, encryptionAlgorithm);
//            log(packet.Type.ToString());
        }

        private void ClientGui_FormClosed(object sender, FormClosedEventArgs e)
        {
            CloseConnection();
        }

        private byte[] buffer;
        private EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.Noencryption;
    }
}
