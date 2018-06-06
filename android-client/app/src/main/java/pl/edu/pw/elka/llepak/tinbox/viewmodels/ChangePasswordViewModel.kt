package pl.edu.pw.elka.llepak.tinbox.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import pl.edu.pw.elka.llepak.tinbox.Connection
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.Connection.errorData
import pl.edu.pw.elka.llepak.tinbox.Connection.reconnect
import pl.edu.pw.elka.llepak.tinbox.protobuf.ResponseType
import pl.edu.pw.elka.llepak.tinbox.tasks.ChangePasswordTask

class ChangePasswordViewModel: ViewModel() {

    lateinit var changePasswordTask: ChangePasswordTask

    val changedPasswordLiveData = MutableLiveData<Boolean>()

    fun changePassword(currentPassword: String, newPassword: String) {
        try {
            when(changePasswordTask.status) {
                AsyncTask.Status.FINISHED -> {
                    changePasswordTask = ChangePasswordTask(currentPassword, newPassword)
                    changePasswordTask.execute()
                }
                AsyncTask.Status.PENDING -> {
                    changePasswordTask.execute()
                }
                AsyncTask.Status.RUNNING -> {
                    return
                }
                else -> {
                    changePasswordTask = ChangePasswordTask(currentPassword, newPassword)
                    changePasswordTask.execute()
                }
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            changePasswordTask = ChangePasswordTask(currentPassword, newPassword)
            changePasswordTask.execute()
        }

        Thread({
            val (response, responseType) = changePasswordTask.get()
            val changedPassword = when(responseType) {
                ResponseType.OK -> {
                    connectionData.postValue("Password successfully changed!")
                    true
                }
                ResponseType.ERROR -> {
                    errorData.postValue(response.getParams(0).sParamVal)
                    false
                }
                else -> {
                    if (reconnect()) {
                        changePassword(currentPassword, newPassword)
                        false
                    }
                    else {
                        Connection.errorData.postValue("Error while logging out.")
                        false
                    }
                }

            }
            changedPasswordLiveData.postValue(changedPassword)
        }).start()
    }
}