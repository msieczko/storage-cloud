package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse

class LogoutTask: AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        val logoutObject = Connection.messageBuilder.buildCommand(CommandType.LOGOUT, mutableListOf())
        var response: ServerResponse
        var responseType: ResponseType = ResponseType.NULL5
        val logout = Thread({
            response = Connection.sendCommandWithResponse(logoutObject)
            responseType = response.type
        })
        logout.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler({ t, e ->
            e.printStackTrace()
        })
        logout.start()
        logout.join()
        return when (responseType) {
            ResponseType.OK -> {
                Connection.connectionData.postValue("Logged out!")
                true
            }
            ResponseType.ERROR -> {
                Connection.connectionData.postValue("Cannot log out!")
                false
            }
            else -> {
                Connection.connectionData.postValue("Connection lost! Reconnecting!")
                Connection.reconnect()
                false
            }
        }
    }
}