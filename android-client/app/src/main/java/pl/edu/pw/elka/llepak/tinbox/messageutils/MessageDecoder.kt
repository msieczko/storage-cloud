package pl.edu.pw.elka.llepak.tinbox.messageutils

import pl.edu.pw.elka.llepak.tinbox.protobuf.EncodedMessage
import pl.edu.pw.elka.llepak.tinbox.protobuf.EncryptionAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.HashAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse
import java.io.InputStream
import java.nio.ByteBuffer

class MessageDecoder {
    private val hashAlgorithm = HashAlgorithm.H_SHA512
    private val encryptionAlgorithm = EncryptionAlgorithm.NOENCRYPTION

    fun decodeMessage(sizeBuffer: ByteArray, data: InputStream): ServerResponse {
        val responseSize = ByteBuffer.wrap(sizeBuffer).int

        val responseByte = ByteArray(responseSize - 4)
        data.read(responseByte, 0, responseSize - 4)
        val encoded = EncodedMessage.parseFrom(responseByte)
        return ServerResponse.parseFrom(encoded.data)
    }
}