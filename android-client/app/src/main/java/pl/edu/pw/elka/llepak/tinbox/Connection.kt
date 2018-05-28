package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.MutableLiveData
import com.google.protobuf.ByteString
import pl.edu.pw.elka.llepak.tinbox.messageutils.MessageBuilder
import pl.edu.pw.elka.llepak.tinbox.messageutils.MessageDecoder
import pl.edu.pw.elka.llepak.tinbox.protobuf.*
import java.net.InetSocketAddress
import java.net.Socket

object Connection {
    const val HOST = "mily20001.ddns.net"
    const val PORT = 52137
    const val TIMEOUT = 5000

    var socket: Socket = Socket()

    val messageBuilder = MessageBuilder()
    val messageDecoder = MessageDecoder()

    val connectionData = MutableLiveData<String>()

    var initialized: Boolean = false

    var sid: ByteString = ByteString.EMPTY
    var username: String = ""

    private fun connect() {
        val connect = Thread({
            val address = InetSocketAddress(HOST, PORT)
            socket.connect(address, TIMEOUT)
        })
        connect.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler({ t, e ->
            e.printStackTrace()
        })
        connect.start()
        connect.join()
    }

    fun initialize() {
        if (initialized) return
        connect()
        if (socket.isConnected) {
            socket.soTimeout = TIMEOUT
            var response: ResponseType = ResponseType.NULL5
            val handshake = Thread({
                response = createAndSendHandshake()
            })
            handshake.start()
            handshake.join()
            connectionData.postValue("Connected to " + socket.inetAddress + ", port" + socket.port + ", " + response.name)
            initialized = true
        }
        else
            connectionData.postValue("Cannot connect to host! Check your internet connection or server status and restart the app!")
    }

    private fun createAndSendHandshake(): ResponseType {
        val handshake = messageBuilder.buildHandshake()
        val encodedMessage = messageBuilder.buildEncodedMessage(MessageType.HANDSHAKE, handshake.toByteArray())

        val toSend = messageBuilder.prepareToSend(encodedMessage)

        socket.getOutputStream().write(toSend)

        val received = socket.getInputStream()
        val receiveBuffer = ByteArray(4)
        received.read(receiveBuffer, 0, 4)

        return messageDecoder.decodeMessage(receiveBuffer, received).type
    }

    fun sendCommandWithResponse(command: Command): ServerResponse {
        val byteData = command.toByteArray()

        val encodedMessage = messageBuilder.buildEncodedMessage(MessageType.COMMAND, byteData)

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

    fun reconnect() {
        socket.close()
        socket = Socket()
        connect()
        if (socket.isConnected) {
            connectionData.postValue("Reconnected!")
            if (sid != ByteString.EMPTY && username != "")
                relogin()
        }
        else
            connectionData.postValue("Connection error!")
    }

    private fun relogin(): Boolean {
        val sidParam = messageBuilder.buildParam("sid", sid)
        val userParam = messageBuilder.buildParam("username", username)

        val command = messageBuilder.buildCommand(CommandType.RELOGIN, mutableListOf(sidParam, userParam))

        var response: ServerResponse = ServerResponse.getDefaultInstance()
        var responseType: ResponseType = ResponseType.NULL5

        val relogin = Thread({
            response = sendCommandWithResponse(command)
            responseType = response.type
        })
        relogin.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler({ t, e ->
            e.printStackTrace()
        })
        relogin.start()
        relogin.join()
        return when(responseType) {
            ResponseType.LOGGED -> {
                connectionData.postValue("Logged in as ${username}")
                sid = response.getParams(0).bParamVal
                true
            }
            ResponseType.ERROR -> {
                connectionData.postValue("Wrong sid or username!")
                false
            }
            else -> {
                false
            }
        }
    }
}