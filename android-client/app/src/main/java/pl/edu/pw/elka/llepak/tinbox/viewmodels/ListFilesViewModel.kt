package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.protobuf.File
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.tasks.ListFilesTask
import pl.edu.pw.elka.llepak.tinbox.tasks.MkdirTask

class ListFilesViewModel: ViewModel() {

    val listFilesLiveData = MutableLiveData<List<File>>()
    val operationLiveData = MutableLiveData<Boolean>()

    lateinit var listFilesTask: ListFilesTask
    lateinit var mkdirTask: MkdirTask

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
                    Connection.reconnect()
                    null
                }
            }
            listFilesLiveData.postValue(list)
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
                AsyncTask.Status.RUNNING -> { }
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
                ResponseType.OK -> Connection.connectionData.postValue("Folder created!")
                ResponseType.ERROR -> {
                    val errorMsg = response.getParams(0).sParamVal
                    Connection.errorData.postValue(errorMsg)
                }
                else -> {
                    Connection.reconnect()
                    Connection.errorData.postValue("Error while creating folder!")
                }
            }
        }).start()
    }
}
