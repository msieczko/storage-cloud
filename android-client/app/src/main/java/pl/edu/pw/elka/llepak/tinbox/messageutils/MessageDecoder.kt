package pl.edu.pw.elka.llepak.tinbox.messageutils

import android.util.Log
import com.google.protobuf.ByteString
import pl.edu.pw.elka.llepak.tinbox.Connection.encryptionAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.EncodedMessage
import pl.edu.pw.elka.llepak.tinbox.protobuf.EncryptionAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.HashAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse
import pl.edu.pw.elka.llepak.tinbox.utils.EncodeUtils
import java.io.InputStream
import java.nio.ByteBuffer

class MessageDecoder {

    fun decodeMessage(sizeBuffer: ByteArray, data: InputStream): ServerResponse {
        val responseSize = ByteBuffer.wrap(sizeBuffer).int

        val responseByte = ByteArray(responseSize - 4)
        var read = 0
        while (read != responseSize - 4)
            read += data.read(responseByte, read, responseSize - read - 4)
        Log.i("diff: ", (responseSize - read).toString())
        val encoded = EncodedMessage.parseFrom(responseByte)

        val decoded = when(encryptionAlgorithm) {
            EncryptionAlgorithm.CAESAR -> EncodeUtils.caesarDecode(encoded.data.toByteArray())
            else -> encoded.data.toByteArray()
        }
        return ServerResponse.parseFrom(ByteString.copyFrom(decoded))
    }
}