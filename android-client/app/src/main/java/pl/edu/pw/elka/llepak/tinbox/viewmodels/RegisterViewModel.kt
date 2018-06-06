package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.util.Log
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.tasks.RegisterTask

class RegisterViewModel: ViewModel() {
    
    lateinit var registerTask: RegisterTask
    val registeredLiveData = MutableLiveData<Boolean>()
    
    fun register(username: String, pass: String, name: String, surname: String) {
        try {
            when(registerTask.status) {
                AsyncTask.Status.FINISHED -> {
                    registerTask = RegisterTask(username, pass, name, surname)
                    registerTask.execute()
                }
                AsyncTask.Status.PENDING -> {
                    registerTask.execute()
                }
                AsyncTask.Status.RUNNING -> {
                    return
                }
                else -> {
                    registerTask = RegisterTask(username, pass, name, surname)
                    registerTask.execute()
                }
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            registerTask = RegisterTask(username, pass, name, surname)
            registerTask.execute()
        }

        Thread({
            val (response, responseType) = registerTask.get()
            val registered = when (responseType) {
                ResponseType.OK -> {
                    Connection.connectionData.postValue("Registered $username")
                    true
                }
                ResponseType.ERROR -> {
                    val errorMsg = response.getParams(0).sParamVal
                    Connection.errorData.postValue(errorMsg)
                    false
                }
                else -> {
                    if (Connection.reconnect()) {
                        registerTask = RegisterTask(username, pass, name, surname)
                        registerTask.execute()
                        if (registerTask.get().second == ResponseType.LOGGED) {
                            Connection.connectionData.postValue("Registered $username")
                            true
                        }
                        else
                            false
                    }
                    else {
                        Connection.errorData.postValue("Error while registering.")
                        false
                    }
                }
            }
            registeredLiveData.postValue(registered)
        }).start()
    }
}