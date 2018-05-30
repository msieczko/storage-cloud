package pl.edu.pw.elka.llepak.tinbox.messageutils

import com.google.protobuf.ByteString
import pl.edu.pw.elka.llepak.tinbox.Connection.encryptionAlgorithm
import pl.edu.pw.elka.llepak.tinbox.Connection.hashAlgorithm
import pl.edu.pw.elka.llepak.tinbox.protobuf.*
import pl.edu.pw.elka.llepak.tinbox.utils.EncodeUtils
import pl.edu.pw.elka.llepak.tinbox.utils.HashUtils
import java.io.ByteArrayOutputStream

class MessageBuilder {

    fun buildParam(id: String, value: String) = Param.newBuilder().setParamId(id).setSParamVal(value).build()

    fun buildParam(id: String, value: Long) = Param.newBuilder().setParamId(id).setIParamVal(value).build()

    fun buildParam(id: String, value: ByteString) = Param.newBuilder().setParamId(id).setBParamVal(value).build()

    fun buildCommand(type: CommandType, params: List<Param>) = Command.newBuilder().setType(type).addAllParams(params).build()

    fun buildHandshake(encryptionAlgorithm: EncryptionAlgorithm) = Handshake.newBuilder().setEncryptionAlgorithm(encryptionAlgorithm).build()

    fun buildEncodedMessage(messageType: MessageType, dataBytes: ByteArray): EncodedMessage {
        val hash = HashUtils.sha512(dataBytes)

        val encryptedDataBytes = when (encryptionAlgorithm) {
            EncryptionAlgorithm.CAESAR -> EncodeUtils.caesarEncode(dataBytes)
            else -> dataBytes
        }

        return EncodedMessage.newBuilder()
                .setData(ByteString.copyFrom(encryptedDataBytes))
                .setDataSize(encryptedDataBytes.size.toLong())
                .setHashAlgorithm(hashAlgorithm)
                .setHash(ByteString.copyFrom(hash))
                .setType(messageType)
                .build()
    }

    fun prepareToSend(message: EncodedMessage): ByteArray {
        val eBytes = message.toByteArray()
        val size = eBytes.size + 4

        val byte3 = (size and 0xFF).toByte()
        val byte2 = ((size shr 8) and 0xFF).toByte()
        val byte1 = ((size shr 16) and 0xFF).toByte()
        val byte0 = ((size shr 24) and 0xFF).toByte()

        val serializedSiz = byteArrayOf(byte0, byte1, byte2, byte3)

        val byteStream = ByteArrayOutputStream()

        byteStream.write(serializedSiz)
        byteStream.write(eBytes)
        return byteStream.toByteArray()
    }
}