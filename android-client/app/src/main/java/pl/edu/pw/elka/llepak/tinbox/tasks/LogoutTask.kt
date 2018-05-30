package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import com.google.protobuf.ByteString
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.Connection.errorData
import pl.edu.pw.elka.llepak.tinbox.Connection.sid
import pl.edu.pw.elka.llepak.tinbox.Connection.username
import pl.edu.pw.elka.llepak.tinbox.R.string.logout
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse
import java.net.SocketTimeoutException

class LogoutTask: AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        val logoutObject = Connection.messageBuilder.buildCommand(CommandType.LOGOUT, mutableListOf())
        val response: ServerResponse
        var responseType: ResponseType = ResponseType.NULL5
        try {
            response = Connection.sendCommandWithResponse(logoutObject)
            responseType = response.type
        }
        catch (e: Exception) {}
        return when (responseType) {
            ResponseType.OK -> {
                username = ""
                sid = ByteString.EMPTY
                true
            }
            ResponseType.ERROR -> false
            else -> {
                Connection.reconnect()
                false
            }

        }
    }
}