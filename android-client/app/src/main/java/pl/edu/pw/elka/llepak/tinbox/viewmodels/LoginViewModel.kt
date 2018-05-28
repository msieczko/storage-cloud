package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.tasks.LoginTask

class LoginViewModel: ViewModel() {

    lateinit var loginTask: LoginTask
    val loggedInLiveData = MutableLiveData<Boolean>()


    fun login(username: String, password: String) {
        try {
            when(loginTask.status) {
                AsyncTask.Status.FINISHED -> {
                    loginTask = LoginTask(username, password)
                    loginTask.execute()
                }
                AsyncTask.Status.PENDING -> {
                    loginTask.execute()
                }
                AsyncTask.Status.RUNNING -> {}
                null -> {
                    loginTask = LoginTask(username, password)
                    loginTask.execute()
                }
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            loginTask = LoginTask(username, password)
            loginTask.execute()
        }

        Thread({
            loggedInLiveData.postValue(loginTask.get())
        }).start()
    }
}