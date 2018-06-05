package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import com.google.protobuf.ByteString
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.protobuf.File
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.tasks.ListFilesTask
import pl.edu.pw.elka.llepak.tinbox.tasks.LogoutTask
import pl.edu.pw.elka.llepak.tinbox.tasks.MkdirTask

class MainMenuViewModel: ViewModel() {

    var logoutTask: LogoutTask = LogoutTask()
    val loggedOutLiveData = MutableLiveData<Boolean>()


    fun logout() {
        when(logoutTask.status) {
            AsyncTask.Status.FINISHED -> {
                logoutTask = LogoutTask()
                logoutTask.execute()
            }
            AsyncTask.Status.PENDING -> {
                logoutTask.execute()
            }
            AsyncTask.Status.RUNNING -> {}
            else -> {
                logoutTask = LogoutTask()
                logoutTask.execute()
            }
        }
        Thread({
            val (response, responseType) = logoutTask.get()
            val loggedOut = when (responseType) {
                ResponseType.OK -> {
                    Connection.username = ""
                    Connection.sid = ByteString.EMPTY
                    Connection.connectionData.postValue("Logged out!")
                    true
                }
                ResponseType.ERROR -> {
                    val errorMsg = response.getParams(0).sParamVal
                    Connection.errorData.postValue(errorMsg)
                    false
                }
                else -> {
                    Connection.reconnect()
                    Connection.errorData.postValue("Error while logging out.")
                    false
                }
            }
            loggedOutLiveData.postValue(loggedOut)
        }).start()
    }
}