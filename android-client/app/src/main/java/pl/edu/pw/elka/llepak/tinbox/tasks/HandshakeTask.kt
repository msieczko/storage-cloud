package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.getHandshakeAnswer
import pl.edu.pw.elka.llepak.tinbox.Connection.messageBuilder
import pl.edu.pw.elka.llepak.tinbox.Connection.messageDecoder
import pl.edu.pw.elka.llepak.tinbox.Connection.reconnect
import pl.edu.pw.elka.llepak.tinbox.Connection.sendHandshake
import pl.edu.pw.elka.llepak.tinbox.protobuf.EncryptionAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType

class HandshakeTask(private val encryptionAlogrithm: EncryptionAlgorithm): AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        val handshake = Connection.messageBuilder.buildHandshake(EncryptionAlgorithm.CAESAR)
        var responseType = ResponseType.NULL5
        try {
            sendHandshake(handshake)
            Connection.encryptionAlgorithm = encryptionAlogrithm
            responseType = getHandshakeAnswer()
        }
        catch (e: Exception) {}
        return when (responseType) {
            ResponseType.OK ->
                true
            ResponseType.ERROR -> false
            else -> {
                Connection.encryptionAlgorithm = EncryptionAlgorithm.NOENCRYPTION
                reconnect()
                false
            }
        }
    }
}