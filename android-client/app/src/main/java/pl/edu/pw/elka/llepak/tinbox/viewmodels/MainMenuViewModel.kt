package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.tasks.LogoutTask

class MainMenuViewModel: ViewModel() {

    lateinit var logoutTask: LogoutTask
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
                null -> {
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
            loggedOutLiveData.postValue(logoutTask.get())
        }).start()
    }
}