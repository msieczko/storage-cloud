package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.util.Log
import com.google.protobuf.ByteString
import pl.edu.pw.elka.llepak.tinbox.messageutils.MessageBuilder
import pl.edu.pw.elka.llepak.tinbox.messageutils.MessageDecoder
import pl.edu.pw.elka.llepak.tinbox.protobuf.*
import pl.edu.pw.elka.llepak.tinbox.tasks.*
import java.net.InetSocketAddress
import java.net.Socket

object Connection {
    const val HOST = "mily20001.ddns.net"
    const val PORT = 52137
    const val TIMEOUT = 5000

    var encryptionAlgorithm = EncryptionAlgorithm.NOENCRYPTION
    val defaulEncryptionAlgorithm = EncryptionAlgorithm.CAESAR

    var connectTask: ConnectTask = ConnectTask()
    var reconnectTask: ReconnectTask = ReconnectTask()
    var reloginTask: ReloginTask = ReloginTask()

    lateinit var listFilesTask: ListFilesTask

    var socket: Socket = Socket()

    val messageBuilder = MessageBuilder()
    val messageDecoder = MessageDecoder()

    val connectionData = MutableLiveData<String>()
    val errorData = MutableLiveData<String>()
    val connectData = MutableLiveData<Boolean>()

    val transferProgressData = MutableLiveData<Int>()

    var initialized: Boolean = false

    var sid: ByteString = ByteString.EMPTY
    var username: String = ""

    fun connect(message: String? = null) {
        when (connectTask.status) {
            AsyncTask.Status.FINISHED -> {
                connectTask = ConnectTask()
                connectTask.execute()
            }
            AsyncTask.Status.PENDING -> {
                connectTask.execute()
            }
            AsyncTask.Status.RUNNING -> {
                return
            }
            else -> {
                connectTask = ConnectTask()
                connectTask.execute()
            }
        }
        Thread({
            val connected = connectTask.get()
            if (connected) {
                connectionData.postValue(message)
                sendHandshake()
            }
            else {
                errorData.postValue("Error while connecting!")
                connectData.postValue(false)
            }
        }).start()
    }

    fun buildCommand(type: CommandType, params: List<Param>): Command {
        return messageBuilder.buildCommand(type, params)
    }

    fun buildParam(paramId: String, paramVal: Any): Param {
        val partParam = Param.newBuilder().setParamId(paramId)
        return when (paramVal) {
            is String -> partParam.setSParamVal(paramVal).build()
            is Long -> partParam.setIParamVal(paramVal).build()
            is ByteArray -> partParam.setBParamVal(ByteString.copyFrom(paramVal)).build()
            else -> Param.getDefaultInstance()
        }
    }


    fun sendHandshake() {
        val handshake = HandshakeTask(defaulEncryptionAlgorithm)
        handshake.execute()
        Thread({
            val (response, responseType) = handshake.get()
            when (responseType) {
                ResponseType.OK -> {
                    connectData.postValue(true)
                    connectionData.postValue("Connection established!!")
                }
                ResponseType.ERROR -> {
                    connectData.postValue(false)
                    val errorMsg = response.getParams(0).sParamVal
                    errorData.postValue(errorMsg)
                }
                else -> {
                    connectData.postValue(false)
                    errorData.postValue("Error while sending handshake!")
                }
            }
        }).start()
    }

    fun buildHandshake(encryptionAlgorithm: EncryptionAlgorithm): Handshake {
        return messageBuilder.buildHandshake(encryptionAlgorithm)
    }

    fun sendHandshake(handshake: Handshake): ServerResponse {
        sendWithoutResponse(MessageType.HANDSHAKE, handshake.toByteArray())
        encryptionAlgorithm = defaulEncryptionAlgorithm
        return getResponse()
    }


    fun sendCommandWithResponse(command: Command): ServerResponse {
        val byteData = command.toByteArray()
        val responded = sendWithResponse(MessageType.COMMAND, byteData)
        return responded
    }

    private fun sendWithResponse(type: MessageType, data: ByteArray): ServerResponse {
        sendWithoutResponse(type, data)
        return getResponse()
    }

    private fun sendWithoutResponse(type: MessageType, data: ByteArray) {
        val encodedMessage = messageBuilder.buildEncodedMessage(type, data)

        val toSend = messageBuilder.prepareToSend(encodedMessage)

        socket.getOutputStream().write(toSend)
    }

    private fun getResponse(): ServerResponse {
        val received = socket.getInputStream()
        val receiveBuffer = ByteArray(4)
        received.read(receiveBuffer, 0, 4)
        return messageDecoder.decodeMessage(receiveBuffer, received)
    }

    fun reconnect(): Boolean {
        when (reconnectTask.status) {
            AsyncTask.Status.FINISHED -> {
                encryptionAlgorithm = EncryptionAlgorithm.NOENCRYPTION
                reconnectTask = ReconnectTask()
                reconnectTask.execute()
            }
            AsyncTask.Status.PENDING -> {
                encryptionAlgorithm = EncryptionAlgorithm.NOENCRYPTION
                reconnectTask.execute()
            }
            AsyncTask.Status.RUNNING -> { }
            else -> {
                encryptionAlgorithm = EncryptionAlgorithm.NOENCRYPTION
                reconnectTask = ReconnectTask()
                reconnectTask.execute()
            }
        }
        val reconnected = reconnectTask.get()
        return if (reconnected) {
            sendHandshake()
            if (sid != ByteString.EMPTY && username != "")
                relogin()
            else {
                connectionData.postValue("Reconnected!")
                true
            }
        }
        else {
            errorData.postValue("Error while reconnecting!")
            false
        }
    }

    private fun relogin(): Boolean {
        when (reloginTask.status) {
            AsyncTask.Status.FINISHED -> {
                reloginTask = ReloginTask()
                reloginTask.execute()
            }
            AsyncTask.Status.PENDING -> {
                reloginTask.execute()
            }
            AsyncTask.Status.RUNNING -> { }
            else -> {
                reloginTask = ReloginTask()
                reloginTask.execute()
            }
        }
        val (response, responseType) = reloginTask.get()
        return when(responseType) {
            ResponseType.LOGGED -> {
                Connection.sid = response.getParams(0).bParamVal
                connectionData.postValue("Logged in as $username")
                true
            }
            ResponseType.ERROR -> {
                val errorMsg = response.getParams(0).sParamVal
                errorData.postValue(errorMsg)
                false
            }
            else -> {
                errorData.postValue("Error while relogging")
                false
            }
        }
    }
}