package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import android.util.Log
import pl.edu.pw.elka.llepak.tinbox.Connection.buildCommand
import pl.edu.pw.elka.llepak.tinbox.Connection.buildParam
import pl.edu.pw.elka.llepak.tinbox.Connection.reconnect
import pl.edu.pw.elka.llepak.tinbox.Connection.sendCommandWithResponse
import pl.edu.pw.elka.llepak.tinbox.Connection.transferProgressData
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.security.MessageDigest

class SendFileTask(private val filepath: String, private val path: String): AsyncTask<Unit, Unit, Boolean>() {

    private val dataBatch = 1024 * 1024

    override fun doInBackground(vararg params: Unit?): Boolean {
        val file = File(filepath)
        val sha1 = MessageDigest.getInstance("SHA1")
        val inputStream = BufferedInputStream(FileInputStream(file))
        val buffer = ByteArray(1024)
        var read = 0
        while (read != -1) {
            read = inputStream.read(buffer)
            if (read > 0)
                sha1.update(buffer, 0, read)
        }
        val hash = sha1.digest()
        val fileSize = file.length()
        val filePathBuilder = StringBuilder()
        filePathBuilder.append(path)
        if (!path.endsWith("/"))
            filePathBuilder.append("/")
        filePathBuilder.append(file.name)
        Log.i("filePath", filePathBuilder.toString())
        val pathParam = buildParam("target_file_path", filePathBuilder.toString())
        val sizeParam = buildParam("size", fileSize)
        val checksumParam = buildParam("file_checksum", hash)
        val metadataObject = buildCommand(CommandType.METADATA, mutableListOf(pathParam, sizeParam, checksumParam))
        var response = ServerResponse.getDefaultInstance()
        var responseType = ResponseType.NULL5
        try {
            response = sendCommandWithResponse(metadataObject)
            responseType = response.type
        }
        catch (e: Exception) {}
        when (responseType) {
            ResponseType.CAN_SEND -> {
                var toRet = true
                val startingChunk = response.getParams(0).iParamVal
                val fileStream = RandomAccessFile(file, "r")
                val chunks = (fileSize + dataBatch - startingChunk) / dataBatch
                var counter = 0
                while (counter < chunks) {
                    if (isCancelled)
                        return false
                    fileStream.seek(startingChunk + counter * dataBatch)
                    val size = if (fileSize - counter * dataBatch - startingChunk >= dataBatch) dataBatch else (fileSize - counter * dataBatch - startingChunk).toInt()
                    val dataBuffer = ByteArray(size)
                    val readSize = fileStream.read(dataBuffer)
                    if (size != readSize)
                        throw RuntimeException()
                    val fileDataParam = buildParam("data", dataBuffer)
                    val sendCommand = buildCommand(CommandType.USR_DATA, mutableListOf(fileDataParam))
                    response = ServerResponse.getDefaultInstance()
                    responseType = ResponseType.NULL5
                    try {
                        response = sendCommandWithResponse(sendCommand)
                        responseType = response.type
                    }
                    catch (e: Exception) {}
                    when (responseType) {
                        ResponseType.OK -> {
                            transferProgressData.postValue(((startingChunk + counter * dataBatch) * 100 / fileSize).toInt())
                        }
                        ResponseType.ERROR -> {
                            toRet = false
                        }
                        else -> {
                            toRet = false
                        }
                    }
                    counter += 1
                    if (!toRet)
                        break
                }
                fileStream.close()
                transferProgressData.postValue(100)
                return toRet
            }
            ResponseType.ERROR -> {
                return false
            }
            else -> {
                return false
            }
        }
    }
}