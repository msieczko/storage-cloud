package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.util.Log
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.Connection.errorData
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
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
                AsyncTask.Status.RUNNING -> {
                    return
                }
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
            val (response, responseType) = loginTask.get()
            val loggedIn = when (responseType) {
                ResponseType.LOGGED -> {
                    Log.i("sid_login", response.getParams(0).bParamVal.toString("UTF-16"))
                    Connection.sid = response.getParams(0).bParamVal
                    Connection.username = username
                    connectionData.postValue("Logged in as $username")
                    true
                }
                ResponseType.ERROR -> {
                    val errorMsg = response.getParams(0).sParamVal
                    errorData.postValue(errorMsg)
                    false
                }
                else -> {
                    if (Connection.reconnect()) {
                        login(username, password)
                        false
                    }
                    else {
                        errorData.postValue("Error while logging in.")
                        false
                    }
                }
            }
            loggedInLiveData.postValue(loggedIn)
        }).start()
    }
}