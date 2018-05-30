namespace StorageCloud.Desktop.GUI
{
    partial class LoggedForm
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
            this.logoutButton = new System.Windows.Forms.Button();
            this.relogButton = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // logoutButton
            // 
            this.logoutButton.Location = new System.Drawing.Point(12, 12);
            this.logoutButton.Name = "logoutButton";
            this.logoutButton.Size = new System.Drawing.Size(124, 23);
            this.logoutButton.TabIndex = 0;
            this.logoutButton.Text = "log out";
            this.logoutButton.UseVisualStyleBackColor = true;
            this.logoutButton.Click += new System.EventHandler(this.logoutButton_Click);
            // 
            // relogButton
            // 
            this.relogButton.Location = new System.Drawing.Point(12, 41);
            this.relogButton.Name = "relogButton";
            this.relogButton.Size = new System.Drawing.Size(124, 23);
            this.relogButton.TabIndex = 1;
            this.relogButton.Text = "relog";
            this.relogButton.UseVisualStyleBackColor = true;
            this.relogButton.Click += new System.EventHandler(this.relogButton_Click);
            // 
            // LoggedForm
            // 
            this.ClientSize = new System.Drawing.Size(284, 261);
            this.Controls.Add(this.relogButton);
            this.Controls.Add(this.logoutButton);
            this.Name = "LoggedForm";
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.Button logoutButton;
        private System.Windows.Forms.Button relogButton;
    }
}