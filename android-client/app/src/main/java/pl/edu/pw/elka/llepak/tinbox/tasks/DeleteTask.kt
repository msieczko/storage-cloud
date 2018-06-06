package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection.buildCommand
import pl.edu.pw.elka.llepak.tinbox.Connection.buildParam
import pl.edu.pw.elka.llepak.tinbox.Connection.sendCommandWithResponse
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse

class DeleteTask(private val path: String): AsyncTask<Unit, Unit, Pair<ServerResponse, ResponseType>>() {

    override fun doInBackground(vararg params: Unit?): Pair<ServerResponse, ResponseType> {
        val pathParam = buildParam("path", path)
        val deleteObject = buildCommand(CommandType.DELETE, mutableListOf(pathParam))
        var response: ServerResponse = ServerResponse.getDefaultInstance()
        var responseType = ResponseType.NULL5
        try {
            response = sendCommandWithResponse(deleteObject)
            responseType = response.type
        }
        catch (e: Exception) {}
        return Pair(response, responseType)
    }
}