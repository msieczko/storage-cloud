using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using StorageCloud.Desktop.Protocol;

namespace Server
{
    public partial class ServerGUI : Form
    {
        public ServerGUI()
        {
            InitializeComponent();
        }

        public void createListener()
        {
            // Create an instance of the TcpListener class.
            TcpListener tcpListener = null;
            IPAddress ipAddress = Dns.GetHostEntry("localhost").AddressList[0];
            try
            {
                // Set the listener on the local IP address
                // and specify the port.
                tcpListener = new TcpListener(ipAddress, 15);
                tcpListener.Start();
                Console.WriteLine("Waiting for a connection...");
            }
            catch (Exception e)
            {
                Console.WriteLine("Error: " + e.ToString());
            }
            while (true)
            {
                // Always use a Sleep call in a while(true) loop
                // to avoid locking up your CPU.
                Thread.Sleep(10);
                // Create a TCP socket.
                // If you ran this server on the desktop, you could use
                // Socket socket = tcpListener.AcceptSocket()
                // for greater flexibility.
                TcpClient tcpClient = tcpListener.AcceptTcpClient();
                // Read the data stream from the client.
                byte[] bytes = new byte[256];
                NetworkStream stream = tcpClient.GetStream();
                stream.Read(bytes, 0, bytes.Length);
                SocketHelper helper = new SocketHelper();
                helper.processMsg(tcpClient, stream, bytes);
            }
        }

        static void Main()
        {
            Application.Run(new ServerGUI());
        }

        private void button1_Click(object sender, EventArgs e)
        {
            this.createListener();
        }
    }
}
