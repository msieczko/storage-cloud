using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using StorageCloud.Desktop.Protocol;

namespace StorageCloud.Desktop.GUI
{
    public partial class LoggedForm : Form
    {
        private PacketManager packetManager;
        private byte[] sid;
        private string username;

        public LoggedForm(PacketManager packetManager, byte[] sid, string username)
        {
            this.packetManager = packetManager;
            this.sid = sid;
            this.username = username;
            InitializeComponent();
        }

        private void Reconnect()
        {
            while (true)
            {
                try
                {
                    Console.WriteLine("Trying to reconnect");
                    packetManager.Connect();
                    Console.WriteLine("Reconnecting succeeded");
                    break;
                }
                catch(SocketException)
                {
                    Console.WriteLine("Reconnecting not succesful");
                }
                Thread.Sleep(2000);
            }
        }

        private void RestoreSession()
        {
            Reconnect();
            packetManager.Relog(sid, username);
        }

        private void logoutButton_Click(object sender, EventArgs e)
        {
            try
            {
                packetManager.Logout();
                Close();
            }
            catch (IOException exception)
            {
                Console.WriteLine(exception.ToString());
                packetManager.Relog(sid, username);
            }   
        }

        private void relogButton_Click(object sender, EventArgs e)
        {
            packetManager.Relog(sid, username);
        }
    }
}
