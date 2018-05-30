using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using StorageCloud.Desktop.Protocol;

namespace StorageCloud.Desktop.GUI
{
    public partial class LoginForm : Form
    {
        private PacketManager packetManager;
        private string username = "";
        private string password = "";

        public string Username
        {
            set { username = value; }
            get { return username; }
        }

        public string Password
        {
            set { password = value; }
            get { return password; }
        }

        public LoginForm(PacketManager packetManager)
        {
            this.packetManager = packetManager;
            InitializeComponent();
        }

        private void loginButton_Click(object sender, EventArgs e)
        {
            DialogResult = DialogResult.OK;
            Username = usernameTextBox.Text;
            Password = passwordTextBox.Text;
        }
    }
}
