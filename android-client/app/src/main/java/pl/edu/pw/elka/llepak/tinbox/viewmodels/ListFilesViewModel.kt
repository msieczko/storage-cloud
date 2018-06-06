package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.Connection.errorData
import pl.edu.pw.elka.llepak.tinbox.Connection.listFilesTask
import pl.edu.pw.elka.llepak.tinbox.Connection.reconnect
import pl.edu.pw.elka.llepak.tinbox.protobuf.File
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.tasks.*

class ListFilesViewModel: ViewModel() {

    val listFilesLiveData = MutableLiveData<List<File>>()
    val fileTransferLiveData = MutableLiveData<Boolean>()
    val mkdirLiveData = MutableLiveData<Boolean>()
    val deletedLiveData = MutableLiveData<Boolean>()
    lateinit var mkdirTask: MkdirTask
    lateinit var sendFileTask: SendFileTask
    lateinit var deleteTask: DeleteTask
    lateinit var downloadFileTask: DownloadFileTask

    fun listFiles(path: String) {
        try {
            when (listFilesTask.status) {
                AsyncTask.Status.FINISHED -> {
                    listFilesTask = ListFilesTask(path)
                    listFilesTask.execute()
                }
                AsyncTask.Status.PENDING -> {
                    listFilesTask.execute()
                }
                AsyncTask.Status.RUNNING -> {
                    return
                }
                else -> {
                    listFilesTask = ListFilesTask(path)
                    listFilesTask.execute()
                }
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            listFilesTask = ListFilesTask(path)
            listFilesTask.execute()
        }

        Thread({
            val (response, responseType) = listFilesTask.get()
            val list = when (responseType) {
                ResponseType.FILES -> {
                    response.fileListList
                }
                ResponseType.ERROR -> {
                    null
                }
                else -> {
                    if (Connection.reconnect()) {
                        listFiles(path)
                    }
                    null
                }
            }
            listFilesLiveData.postValue(list)
        }).start()
    }

    fun delete(path: String) {
        try {
            when (deleteTask.status) {
                AsyncTask.Status.FINISHED -> {
                    deleteTask = DeleteTask(path)
                    deleteTask.execute()
                }
                AsyncTask.Status.PENDING -> {
                    deleteTask.execute()
                }
                AsyncTask.Status.RUNNING -> {
                    return
                }
                else -> {
                    deleteTask = DeleteTask(path)
                    deleteTask.execute()
                }
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            deleteTask = DeleteTask(path)
            deleteTask.execute()
        }

        Thread({
            val (response, responseType) = deleteTask.get()
            when (responseType) {
                ResponseType.OK -> {
                    deletedLiveData.postValue(true)
                }
                ResponseType.ERROR -> {
                    deletedLiveData.postValue(false)
                    errorData.postValue(response.getParams(0).sParamVal)
                }
                else -> {
                    if (reconnect())
                        delete(path)
                    else
                        errorData.postValue("Error while deleting!")
                }
            }
        }).start()
    }

    fun mkdir(path: String) {
        try {
            when(mkdirTask.status) {
                AsyncTask.Status.FINISHED -> {
                    mkdirTask = MkdirTask(path)
                    mkdirTask.execute()
                }
                AsyncTask.Status.PENDING -> {
                    mkdirTask.execute()
                }
                AsyncTask.Status.RUNNING -> {
                    return
                }
                else -> {
                    mkdirTask = MkdirTask(path)
                    mkdirTask.execute()
                }
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            mkdirTask = MkdirTask(path)
            mkdirTask.execute()
        }

        Thread({
            val (response, responseType) = mkdirTask.get()
            when(responseType) {
                ResponseType.OK -> {
                    Connection.connectionData.postValue("Folder created!")
                    mkdirLiveData.postValue(true)
                }
                ResponseType.ERROR -> {
                    val errorMsg = response.getParams(0).sParamVal
                    Connection.errorData.postValue(errorMsg)
                }
                else -> {
                    if (Connection.reconnect())
                        mkdir(path)
                    else
                        Connection.errorData.postValue("Error while creating folder!")
                }
            }
        }).start()
    }

    fun uploadFile(filepath: String, path: String) {
        try {
            when (sendFileTask.status) {
                AsyncTask.Status.RUNNING -> {
                    return
                }

                AsyncTask.Status.FINISHED -> {
                    sendFileTask = SendFileTask(filepath, path)
                    sendFileTask.execute()
                }
                AsyncTask.Status.PENDING -> {
                    sendFileTask.execute()
                }
                else -> {
                    sendFileTask = SendFileTask(filepath, path)
                    sendFileTask.execute()
                }
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            sendFileTask = SendFileTask(filepath, path)
            sendFileTask.execute()
        }

        Thread({
            val sent = sendFileTask.get()
            if (sent) {
                fileTransferLiveData.postValue(true)
                connectionData.postValue("File sent!")
            }
            else {
                if (reconnect()) {
                    uploadFile(filepath, path)
                }
                else {
                    fileTransferLiveData.postValue(false)
                    connectionData.postValue("Error while sending file!")
                }
            }
        }).start()
    }
    
    fun download(file: File) {
        try {
            when (downloadFileTask.status) {
                AsyncTask.Status.RUNNING -> {
                    return
                }

                AsyncTask.Status.FINISHED -> {
                    downloadFileTask = DownloadFileTask(file)
                    downloadFileTask.execute()
                }
                AsyncTask.Status.PENDING -> {
                    downloadFileTask.execute()
                }
                else -> {
                    downloadFileTask = DownloadFileTask(file)
                    downloadFileTask.execute()
                }
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            downloadFileTask = DownloadFileTask(file)
            downloadFileTask.execute()
        }

        Thread({
            val downloaded = downloadFileTask.get()
            if (downloaded) {
                fileTransferLiveData.postValue(true)
                connectionData.postValue("File downloaded!")
            }
            else {
                if (reconnect()) {
                    download(file)
                }
                else {
                    fileTransferLiveData.postValue(false)
                    connectionData.postValue("Error while downloading file!")
                }
            }
        }).start()
    }
}
