package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection.buildCommand
import pl.edu.pw.elka.llepak.tinbox.Connection.buildParam
import pl.edu.pw.elka.llepak.tinbox.Connection.sendCommandWithResponse
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.Param
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse

class ChangePasswordTask(private val currentPassword: String, private val newPassword: String): AsyncTask<Unit, Unit, Pair<ServerResponse, ResponseType>>() {

    override fun doInBackground(vararg params: Unit?): Pair<ServerResponse, ResponseType> {
        val currPass = buildParam("current_passwd", currentPassword)
        val newPass = buildParam("new_passwd", newPassword)
        val changeObject = buildCommand(CommandType.CHANGE_PASSWD, mutableListOf(currPass, newPass))
        var response = ServerResponse.getDefaultInstance()
        var responseType = ResponseType.NULL5
        try {
            response = sendCommandWithResponse(changeObject)
            responseType = response.type
        }
        catch (e: Exception) {}
        return Pair(response, responseType)
    }
}