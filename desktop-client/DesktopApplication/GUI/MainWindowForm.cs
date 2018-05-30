using System;
using System.Windows.Forms;
using Google.Protobuf;
using StorageCloud.Desktop.Protocol;

namespace StorageCloud.Desktop.GUI
{
    public partial class MainWindowForm : Form
    {
        public MainWindowForm()
        {
            InitializeComponent();
        }

        private void MainWindowForm_Load(object sender, EventArgs e)
        {
            string serverIP = "mily20001.ddns.net";
            Int32 port = 52137;
            packetManager = new PacketManager(serverIP, port);
        }

        private void connectButton_Click(object sender, EventArgs e)
        {
            packetManager.Connect();
        }

        private void handshakeButton_Click(object sender, EventArgs e)
        {
            ServerResponse response = packetManager.Handshake();
        }

        private void loginButton_Click(object sender, EventArgs e)
        {
            LoginForm loginForm = new LoginForm(packetManager);
            SetVisibleCore(false);
            DialogResult result = loginForm.ShowDialog();
            if (result == DialogResult.OK)
            {
                string username = loginForm.Username;
                string password = loginForm.Password;
                Console.WriteLine("Trying to login with: " + username + " " + password);
                ServerResponse response = packetManager.Login(username, password);
                if (response.Type == ResponseType.Logged)
                {
                    byte[] sid = response.Params[0].ToByteArray();
                    Console.WriteLine("Logged in succesfully");
                    LoggedForm loggedForm = new LoggedForm(packetManager, sid, username);
                    SetVisibleCore(false);
                    loggedForm.ShowDialog();
                    SetVisibleCore(true);
                }
                else
                {
                    Console.WriteLine("Couldn't log in: " + response.List[0]);
                }
            }
            SetVisibleCore(true);
        }

        private PacketManager packetManager;
    }
}
