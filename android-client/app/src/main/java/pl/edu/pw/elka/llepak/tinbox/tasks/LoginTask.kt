package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse

class LoginTask(private val username: String, private val password: String): AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        val usernameParam = Connection.messageBuilder.buildParam("username", username)
        val passwordParam = Connection.messageBuilder.buildParam("password", password)
        val loginObject = Connection.messageBuilder.buildCommand(CommandType.LOGIN, mutableListOf(usernameParam, passwordParam))
        var response: ServerResponse = ServerResponse.getDefaultInstance()
        var responseType: ResponseType = ResponseType.NULL5
        val login = Thread({
            response = Connection.sendCommandWithResponse(loginObject)
            responseType = response.type
        })
        login.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler({ t, e ->
            e.printStackTrace()
        })
        login.start()
        login.join()
        return when (responseType) {
            ResponseType.LOGGED -> {
                connectionData.postValue("Logged in as $username")
                Connection.sid = response.getParams(0).bParamVal
                Connection.username = username
                true
            }
            ResponseType.ERROR -> {
                connectionData.postValue("Wrong username or password!")
                false
            }
            else -> {
                connectionData.postValue("Connection lost! Reconnecting!")
                Connection.reconnect()
                false
            }
        }
    }
}
