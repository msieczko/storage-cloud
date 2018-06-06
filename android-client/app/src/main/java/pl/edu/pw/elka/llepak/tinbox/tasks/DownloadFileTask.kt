package pl.edu.pw.elka.llepak.tinbox.tasks

import android.os.AsyncTask
import android.os.Environment
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.buildCommand
import pl.edu.pw.elka.llepak.tinbox.Connection.buildParam
import pl.edu.pw.elka.llepak.tinbox.Connection.sendCommandWithResponse
import pl.edu.pw.elka.llepak.tinbox.Connection.transferProgressData
import pl.edu.pw.elka.llepak.tinbox.protobuf.CommandType
import pl.edu.pw.elka.llepak.tinbox.protobuf.File
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.protobuf.ServerResponse
import java.io.RandomAccessFile

class DownloadFileTask(private val file: File): AsyncTask<Unit, Unit, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        val pathParam = buildParam("file_path", file.filename)
        val chunkParam = buildParam("starting_chunk", 0L)
        val downloadObject = buildCommand(CommandType.DOWNLOAD, mutableListOf(pathParam, chunkParam))
        var response = ServerResponse.getDefaultInstance()
        var responseType = ResponseType.NULL5
        try {
            response = sendCommandWithResponse(downloadObject)
            responseType = response.type
        }
        catch (e: Exception) {}
        when (responseType) {
            ResponseType.SRV_DATA -> {
                var saved: Long = 0
                val savePath = Environment.getExternalStorageDirectory().path + "/TINBox" + file.filename
                val saveFile = java.io.File(savePath)
                saveFile.mkdirs()
                if (saveFile.exists())
                    saveFile.delete()
                saveFile.createNewFile()
                saveFile.setWritable(true)
                val randomAccessFile = RandomAccessFile(saveFile, "rw")
                randomAccessFile.write(response.data.toByteArray())
                saved += response.data.size()
                var toRet = true
                while (saved != file.size) {
                    val partObject = buildCommand(CommandType.C_DOWNLOAD, mutableListOf())
                    response = ServerResponse.getDefaultInstance()
                    responseType = ResponseType.NULL5
                    try {
                        response = sendCommandWithResponse(partObject)
                        responseType = response.type
                    }
                    catch (e: Exception) { }
                    when (responseType) {
                        ResponseType.SRV_DATA -> {
                            randomAccessFile.write(response.data.toByteArray())
                            saved += response.data.size()
                            transferProgressData.postValue((saved * 100 / file.size).toInt())
                        }
                        ResponseType.ERROR -> {
                            saveFile.delete()
                            toRet = false
                        }
                        else -> {
                            for (r in 0 until 3) {
                                if (Connection.reconnect()) {
                                    toRet = true
                                    break
                                } else {
                                    toRet = false
                                }
                            }
                        }
                    }
                    if (!toRet)
                        break
                }
                return toRet
            }
            ResponseType.ERROR -> {
                return false
            }
            else -> {
                return if (Connection.reconnect())
                    doInBackground()
                else
                    false
            }
        }
    }
}