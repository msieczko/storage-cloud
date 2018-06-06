package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*
import pl.edu.pw.elka.llepak.tinbox.viewmodels.RegisterViewModel

class RegisterActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_progress.visibility = View.GONE

        val viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)

        Connection.connectionData.observe(this, Observer(this::displayConnectionData))
        Connection.errorData.observe(this, Observer(this::displayError))

        viewModel.registeredLiveData.observe(this, Observer(this::isRegistered))

        register_button.setOnClickListener({
            register_progress.visibility = View.VISIBLE
            viewModel.register(register_username.text.toString(), register_password.text.toString(),
                    register_name.text.toString(), register_surname.text.toString())
        })
    }

    private fun isRegistered(registered: Boolean?) {
        if (registered == true) {
            finish()
        }
        else {
            register_progress.visibility = View.GONE
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
