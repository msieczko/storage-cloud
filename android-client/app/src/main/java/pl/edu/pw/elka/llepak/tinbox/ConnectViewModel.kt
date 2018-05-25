package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import java.net.InetSocketAddress
import java.net.Socket

class ConnectViewModel: ViewModel() {

    private var socket: Socket = Socket()

    private val connectData = MutableLiveData<String>()
    private val socketData = MutableLiveData<Socket>()
    private val errorData = MutableLiveData<String>()

    init {
        val connect = Thread({
            try {
                val address = InetSocketAddress(HOST, PORT)
                socket.connect(address, TIMEOUT)
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
        })
        connect.start()
        connect.join()
        if (socket.isConnected) {
            socketData.value = socket
            connectData.value = "Connected to " + socket.inetAddress + ", port" + socket.port
        }
        else
            connectData.value = "Cannot connect to host!"
    }

    companion object {
        const val HOST = "mily20001.ddns.net"
        const val PORT = 52137
        const val TIMEOUT = 5000
    }

    fun connect(): LiveData<String> = connectData
    fun error(): LiveData<String> = errorData


    fun sendMessage(message: String) {
        val socketConnection = SocketConnection(socket)
        socketConnection.execute(message)
        connectData.value = socketConnection.get()
    }


}