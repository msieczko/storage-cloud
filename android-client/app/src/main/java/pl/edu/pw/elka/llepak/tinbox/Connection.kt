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
        }).start()
    }


    fun sendHandshake() {
        val handshake = HandshakeTask()
        handshake.execute()
        Thread({
            connectData.postValue(handshake.get())
        }).start()
    }

    fun sendHandshakeWithResponse(handshake: Handshake): ServerResponse {
        return sendWithResponse(MessageType.HANDSHAKE, handshake.toByteArray())
    }


    fun sendCommandWithResponse(command: Command): ServerResponse {
        val byteData = command.toByteArray()
        val responded = sendWithResponse(MessageType.COMMAND, byteData)
        return responded
    }

    private fun sendWithResponse(type: MessageType, data: ByteArray): ServerResponse {
        val encodedMessage = messageBuilder.buildEncodedMessage(type, data)

        val toSend = messageBuilder.prepareToSend(encodedMessage)

        socket.getOutputStream().write(toSend)

        val received = socket.getInputStream()
        val receiveBuffer = ByteArray(4)
        received.read(receiveBuffer, 0, 4)
        return messageDecoder.decodeMessage(receiveBuffer, received)
    }

    fun sendCommandWithoutResponse(command: Command) {
        val byteData = command.toByteArray()

        val encodedMessage = messageBuilder.buildEncodedMessage(MessageType.COMMAND, byteData)

        val toSend = messageBuilder.prepareToSend(encodedMessage)

        socket.getOutputStream().write(toSend)
    }

    fun reconnect(){
        when (reconnectTask.status) {
            AsyncTask.Status.FINISHED -> {
                reconnectTask = ReconnectTask()
                reconnectTask.execute()
            }
            AsyncTask.Status.PENDING -> {
                reconnectTask.execute()
            }
            AsyncTask.Status.RUNNING -> { }
            else -> {
                reconnectTask = ReconnectTask()
                reconnectTask.execute()
            }
        }
        Thread({
            val reconnected = reconnectTask.get()
            if (reconnected) {
                Log.i("sid", sid.toString("UTF-16"))
                Log.i("username", username)
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
            val logged = reloginTask.get()
            if (logged)
                connectionData.postValue("Logged in as $username")
            else
                errorData.postValue("Error while relogging")
        }).start()
    }
}