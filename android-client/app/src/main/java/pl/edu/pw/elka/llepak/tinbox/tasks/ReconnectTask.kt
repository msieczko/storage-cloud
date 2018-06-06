package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.TIMEOUT
import pl.edu.pw.elka.llepak.tinbox.Connection.connect
import pl.edu.pw.elka.llepak.tinbox.Connection.socket
import java.net.InetSocketAddress
import java.net.Socket

class ReconnectTask: AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        socket.close()
        socket = Socket()
        val address = InetSocketAddress(Connection.HOST, Connection.PORT)
        try {
            socket.connect(address, Connection.TIMEOUT)
        }
        catch (e: Exception) { return false }
        return if (socket.isConnected) {
            socket.soTimeout = Connection.TIMEOUT
            true
        }
        else false
    }
}