package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import android.util.Log
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.buildCommand
import pl.edu.pw.elka.llepak.tinbox.Connection.buildParam
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse
import java.net.SocketTimeoutException

class LoginTask(private val username: String, private val password: String): AsyncTask<Unit, Unit, Pair<ServerResponse, ResponseType>>() {

    override fun doInBackground(vararg params: Unit?): Pair<ServerResponse, ResponseType> {
        val usernameParam = buildParam("username", username)
        val passwordParam = buildParam("password", password)
        val loginObject = buildCommand(CommandType.LOGIN, mutableListOf(usernameParam, passwordParam))
        var response: ServerResponse = ServerResponse.getDefaultInstance()
        var responseType: ResponseType = ResponseType.NULL5
        try {
            response = Connection.sendCommandWithResponse(loginObject)
            responseType = response.type
        }
        catch (e: Exception) {}
        return Pair(response, responseType)
    }
}
