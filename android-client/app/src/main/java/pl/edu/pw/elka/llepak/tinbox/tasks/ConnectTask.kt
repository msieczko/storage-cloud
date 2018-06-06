package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection.HOST
import pl.edu.pw.elka.llepak.tinbox.Connection.PORT
import pl.edu.pw.elka.llepak.tinbox.Connection.TIMEOUT
import pl.edu.pw.elka.llepak.tinbox.Connection.socket
import java.net.InetSocketAddress

class ConnectTask: AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        val address = InetSocketAddress(HOST, PORT)
        try {
            socket.connect(address, TIMEOUT)
        }
        catch (e: Exception) { return false }
        return if (socket.isConnected) {
            socket.soTimeout = TIMEOUT
            true
        }
        else false
    }
}
