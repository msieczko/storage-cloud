package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.tasks.LogoutTask

class MainMenuViewModel: ViewModel() {

    var logoutTask: LogoutTask = LogoutTask()
    val loggedOutLiveData = MutableLiveData<Boolean>()


    fun logout() {
        try {
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
        }
        catch (e: UninitializedPropertyAccessException) {
            logoutTask = LogoutTask()
            logoutTask.execute()
        }

        Thread({
            val loggedOut = logoutTask.get()
            loggedOutLiveData.postValue(loggedOut)
            if (loggedOut)
                Connection.connectionData.postValue("Logged out!")
            else
                Connection.errorData.postValue("Error while logging out!")
        }).start()
    }

    fun getListFiles() {

    }
}