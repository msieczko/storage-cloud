package pl.edu.pw.elka.llepak.tinbox.messageutils

import android.util.Log
import com.google.protobuf.ByteString
import pl.edu.pw.elka.llepak.tinbox.Connection.encryptionAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.EncodedMessage
import pl.edu.pw.elka.llepak.tinbox.protobuf.EncryptionAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.HashAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse
import pl.edu.pw.elka.llepak.tinbox.utils.EncodeUtils
import pl.edu.pw.elka.llepak.tinbox.utils.HashUtils
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

        val hash = when(encoded.hashAlgorithm) {
            HashAlgorithm.H_SHA512 -> HashUtils.sha512(decoded)
            HashAlgorithm.NULL2 -> throw Exception()
            HashAlgorithm.H_NOHASH -> ByteArray(0)
            HashAlgorithm.H_SHA256 -> HashUtils.sha256(decoded)
            HashAlgorithm.H_SHA1 -> HashUtils.sha1(decoded)
            HashAlgorithm.H_MD5 -> throw Exception()
            HashAlgorithm.UNRECOGNIZED -> throw Exception()
            else -> throw Exception()
        }

        if (!hash.contentEquals(encoded.hash.toByteArray()))
            throw Exception()


        return ServerResponse.parseFrom(ByteString.copyFrom(decoded))
    }
}