package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import android.util.Log
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse
import java.net.SocketTimeoutException

class LoginTask(private val username: String, private val password: String): AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        val usernameParam = Connection.messageBuilder.buildParam("username", username)
        val passwordParam = Connection.messageBuilder.buildParam("password", password)
        val loginObject = Connection.messageBuilder.buildCommand(CommandType.LOGIN, mutableListOf(usernameParam, passwordParam))
        var response: ServerResponse = ServerResponse.getDefaultInstance()
        var responseType: ResponseType = ResponseType.NULL5
        try {
            response = Connection.sendCommandWithResponse(loginObject)
            responseType = response.type
        }
        catch (e: Exception) {}
        return when (responseType) {
            ResponseType.LOGGED -> {
                Log.i("sid_login", response.getParams(0).bParamVal.toString("UTF-16"))
                Connection.sid = response.getParams(0).bParamVal
                Connection.username = username
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
