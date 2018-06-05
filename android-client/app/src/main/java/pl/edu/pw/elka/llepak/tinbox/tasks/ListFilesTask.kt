package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection.messageBuilder
import pl.edu.pw.elka.llepak.tinbox.Connection.reconnect
import pl.edu.pw.elka.llepak.tinbox.Connection.sendCommandWithResponse
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.File
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse

class ListFilesTask(private val path: String): AsyncTask<Unit, Unit, Pair<ServerResponse, ResponseType>>() {

    override fun doInBackground(vararg params: Unit?): Pair<ServerResponse, ResponseType> {
        val pathParam = messageBuilder.buildParam("path", path)
        val pathCommand = messageBuilder.buildCommand(CommandType.LIST_FILES, mutableListOf(pathParam))
        var response: ServerResponse = ServerResponse.getDefaultInstance()
        var responseType: ResponseType = ResponseType.NULL5
        try {
            response = sendCommandWithResponse(pathCommand)
            responseType = response.type
        }
        catch (e: Exception) {}
        return Pair(response, responseType)
    }
}