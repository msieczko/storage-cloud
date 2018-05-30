package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.Connection.errorData
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
                else -> {
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
            val loggedIn = loginTask.get()
            loggedInLiveData.postValue(loggedIn)
            if (loggedIn)
                connectionData.postValue("Logged in as $username")
            else
                errorData.postValue("Error while logging in!")
        }).start()
    }
}