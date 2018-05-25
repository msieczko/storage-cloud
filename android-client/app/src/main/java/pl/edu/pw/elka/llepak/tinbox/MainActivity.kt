package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.arch.lifecycle.Observer
import java.net.Socket
import pl.edu.pw.elka.llepak.tinbox.protbuf.Messages

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ConnectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(ConnectViewModel::class.java)

        viewModel.connect().observe(this, Observer(this::displayConnectData))
        viewModel.error().observe(this, Observer(this::displayError))

        message_button.setOnClickListener{
//            Thread({
//                try {
//                    if (!viewModel.socket.isClosed)
//                        viewModel.sendMessage(message.text.toString())
//                    else
//                        Toast.makeText(this, "Socket is closed!", Toast.LENGTH_SHORT).show()
//                }
//                catch (e: Exception) {
//                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
//                }
//            }).run()
        }

//        button_connect.setOnClickListener({
//            Thread({
//                try {
//                    viewModel.createSocket()
//                }
//                catch (e: Exception) {
//                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
//                }
//            }).run()
//            viewModel.socketCreator().socketData().observe(this, Observer(this::bindSocket))
//        })

//        button_disconnect.setOnClickListener({
//            try {
//            viewModel.socket.close()
//            socket_status.text = this.getText(R.string.socket_disconnected)
//            }
//            catch (e: Exception) {
//                Toast.makeText(this, "Cannot disconnect unconnected socket!", Toast.LENGTH_SHORT).show()
//            }
//
//        })
//
//        try {
//            if (viewModel.socket.isConnected)
//                socket_status.text = this.getText(R.string.socket_connected)
//            else
//                socket_status.text = this.getText(R.string.socket_disconnected)
//        }
//        catch (e: Exception) {
//            socket_status.text = this.getText(R.string.socket_disconnected)
//        }
    }

    private fun bindSocket(socket: Socket?) {
//        val connected = socket?.isConnected ?: false
//        if (!connected)
//            Toast.makeText(this, "Cannot connect to server", Toast.LENGTH_SHORT).show()
//        else {
//            viewModel.socket = socket ?: return
//            socket_status.text = this.getText(R.string.socket_connected)
//            Toast.makeText(this, "Connected to " + viewModel.socket.inetAddress + ", port " + viewModel.socket.port, Toast.LENGTH_SHORT).show()
//        }
    }

    private fun displayConnectData(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun displayError(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
//        try {
//            viewModel.socket.close()
//            socket_status.text = this.getText(R.string.socket_disconnected)
//        }
//        catch (e: Exception) {
//
//        }
    }
}
