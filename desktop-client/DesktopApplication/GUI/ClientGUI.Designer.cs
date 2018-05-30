namespace StorageCloud.Desktop.Protocol
{
    partial class ClientGui
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.button1 = new System.Windows.Forms.Button();
            this.handshakeButton = new System.Windows.Forms.Button();
            this.loginButton = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(12, 12);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(120, 57);
            this.button1.TabIndex = 0;
            this.button1.Text = "connect to server";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // handshakeButton
            // 
            this.handshakeButton.Location = new System.Drawing.Point(12, 75);
            this.handshakeButton.Name = "handshakeButton";
            this.handshakeButton.Size = new System.Drawing.Size(120, 57);
            this.handshakeButton.TabIndex = 1;
            this.handshakeButton.Text = "handshake";
            this.handshakeButton.UseVisualStyleBackColor = true;
            this.handshakeButton.Click += new System.EventHandler(this.handshakeButton_Click);
            // 
            // loginButton
            // 
            this.loginButton.Location = new System.Drawing.Point(12, 138);
            this.loginButton.Name = "loginButton";
            this.loginButton.Size = new System.Drawing.Size(120, 57);
            this.loginButton.TabIndex = 2;
            this.loginButton.Text = "login";
            this.loginButton.UseVisualStyleBackColor = true;
            this.loginButton.Click += new System.EventHandler(this.loginButton_Click);
            // 
            // ClientGui
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(277, 269);
            this.Controls.Add(this.loginButton);
            this.Controls.Add(this.handshakeButton);
            this.Controls.Add(this.button1);
            this.Name = "ClientGui";
            this.Text = "ClientGUI";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.ClientGui_FormClosing);
            this.Load += new System.EventHandler(this.ClientGui_Load);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button handshakeButton;
        private System.Windows.Forms.Button loginButton;
    }
}