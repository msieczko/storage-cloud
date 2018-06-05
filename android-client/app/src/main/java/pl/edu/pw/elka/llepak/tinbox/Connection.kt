package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.util.Log
import com.google.protobuf.ByteString
import pl.edu.pw.elka.llepak.tinbox.messageutils.MessageBuilder
import pl.edu.pw.elka.llepak.tinbox.messageutils.MessageDecoder
import pl.edu.pw.elka.llepak.tinbox.protobuf.*
import pl.edu.pw.elka.llepak.tinbox.tasks.ConnectTask
import pl.edu.pw.elka.llepak.tinbox.tasks.HandshakeTask
import pl.edu.pw.elka.llepak.tinbox.tasks.ReconnectTask
import pl.edu.pw.elka.llepak.tinbox.tasks.ReloginTask
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

    var socket: Socket = Socket()

    val messageBuilder = MessageBuilder()
    val messageDecoder = MessageDecoder()

    val connectionData = MutableLiveData<String>()
    val errorData = MutableLiveData<String>()
    val connectData = MutableLiveData<Boolean>()

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
            AsyncTask.Status.RUNNING -> { }
            else -> {
                connectTask = ConnectTask()
                connectTask.execute()
            }
        }
        Thread({
            val connected = connectTask.get()
            connectData.postValue(connected)
            if (connected)
                connectionData.postValue(message)
            else
                errorData.postValue("Error while connecting!")
        }).start()
    }


    fun sendHandshake() {
        val handshake = HandshakeTask(defaulEncryptionAlgorithm)
        handshake.execute()
        Thread({
            val (response, responseType) = handshake.get()
            when (responseType) {
                ResponseType.OK -> {
                    connectData.postValue(true)
                    connectionData.postValue("Handshake successful!")
                }
                ResponseType.ERROR -> {
                    connectData.postValue(false)
                    val errorMsg = response.getParams(0).sParamVal
                    errorData.postValue(errorMsg)
                }
            }
        }).start()
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

    fun reconnect() {
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
        Thread({
            val reconnected = reconnectTask.get()
            if (reconnected) {
                sendHandshake()
                if (sid != ByteString.EMPTY && username != "")
                    relogin()
                else
                    connectionData.postValue("Reconnected!")
            }
            else
                errorData.postValue("Error while reconnecting!")
        }).start()
    }

    private fun relogin() {
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
        Thread({
            val (response, responseType) = reloginTask.get()
            when(responseType) {
                ResponseType.LOGGED -> {
                    Connection.sid = response.getParams(0).bParamVal
                    connectionData.postValue("Logged in as $username")
                }
                ResponseType.ERROR -> {
                    val errorMsg = response.getParams(0).sParamVal
                    errorData.postValue(errorMsg)
                }
                else -> errorData.postValue("Error while relogging")
            }
        }).start()
    }
}