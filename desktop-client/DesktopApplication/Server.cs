using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace StorageCloud.Desktop
{
    class Server
    {
        public void createListener()
        {
            // Create an instance of the TcpListener class.
            TcpListener tcpListener = null;
            IPAddress ipAddress = Dns.GetHostEntry("localhost").AddressList[0];
            try
            {
                // Set the listener on the local IP address
                // and specify the port.
                tcpListener = new TcpListener(ipAddress, 13);
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
    }
}
