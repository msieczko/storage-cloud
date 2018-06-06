package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_change_password.*
import pl.edu.pw.elka.llepak.tinbox.viewmodels.ChangePasswordViewModel

class ChangePasswordActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        change_password_progress.visibility = View.GONE

        val viewModel = ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)

        Connection.connectionData.observe(this, Observer(this::displayConnectionData))
        Connection.errorData.observe(this, Observer(this::displayError))

        viewModel.changedPasswordLiveData.observe(this, Observer(this::passwordChanged))

        change_passw_button.setOnClickListener({
            change_password_progress.visibility = View.VISIBLE
            viewModel.changePassword(current_password.text.toString(), new_password.text.toString())
        })
    }

    private fun passwordChanged(isChanged: Boolean?) {
        if (isChanged == true) {
            finish()
        }
        else {
            change_password_progress.visibility = View.GONE
        }
    }

    private fun displayConnectionData(message: String?) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Connection.connectionData.value = null
        }
    }

    private fun displayError(message: String?) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Connection.errorData.value = null
        }
    }
}
