package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.sendHandshake
import pl.edu.pw.elka.llepak.tinbox.protobuf.EncryptionAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse

class HandshakeTask(private val encryptionAlgorithm: EncryptionAlgorithm): AsyncTask<Unit, Unit, Pair<ServerResponse, ResponseType>>() {

    override fun doInBackground(vararg params: Unit?): Pair<ServerResponse, ResponseType> {
        val handshake = Connection.messageBuilder.buildHandshake(EncryptionAlgorithm.CAESAR)
        var response: ServerResponse = ServerResponse.getDefaultInstance()
        var responseType = ResponseType.NULL5
        try {
            response = sendHandshake(handshake)
            responseType = response.type
        }
        catch (e: Exception) {}
        return Pair(response, responseType)
    }
}