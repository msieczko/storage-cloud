package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.reconnect
import pl.edu.pw.elka.llepak.tinbox.Connection.sendHandshakeWithResponse
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType

class HandshakeTask: AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        val handshake = Connection.messageBuilder.buildHandshake()
        var responseType = ResponseType.NULL5
        try {
            responseType = sendHandshakeWithResponse(handshake).type
        }
        catch (e: Exception) {}
        return when (responseType) {
            ResponseType.OK -> true
            ResponseType.ERROR -> false
            else -> {
                reconnect()
                false
            }
        }
    }
}