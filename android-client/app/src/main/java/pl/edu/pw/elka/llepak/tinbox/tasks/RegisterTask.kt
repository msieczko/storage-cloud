package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection.buildCommand
import pl.edu.pw.elka.llepak.tinbox.Connection.buildParam
import pl.edu.pw.elka.llepak.tinbox.Connection.sendCommandWithResponse
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse

class RegisterTask(private val username: String,
                   private val password: String,
                   private val name: String,
                   private val surname: String): AsyncTask<Unit, Unit, Pair<ServerResponse, ResponseType>>()
{

    override fun doInBackground(vararg params: Unit?): Pair<ServerResponse, ResponseType> {
        val usernameParam = buildParam("username", username)
        val passParam = buildParam("pass", password)
        val nameParam = buildParam("first_name", name)
        val surnameParam = buildParam("last_name", surname)
        val registerObject = buildCommand(CommandType.REGISTER, mutableListOf(usernameParam, passParam, nameParam, surnameParam))
        var response: ServerResponse = ServerResponse.getDefaultInstance()
        var responseType = ResponseType.NULL5
        try {
            response = sendCommandWithResponse(registerObject)
            responseType = response.type
        }
        catch (e: Exception) {}
        return Pair(response, responseType)
    }
}