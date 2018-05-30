package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import pl.edu.pw.elka.llepak.tinbox.Connection.connectData
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.Connection.errorData
import pl.edu.pw.elka.llepak.tinbox.Connection.initialized
import pl.edu.pw.elka.llepak.tinbox.Connection.sendHandshake
import pl.edu.pw.elka.llepak.tinbox.viewmodels.LoginViewModel

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_progress.visibility = View.GONE

        connectionData.observe(this, Observer(this::displayConnectionData))
        errorData.observe(this, Observer(this::displayError))

        connectData.observe(this, Observer(this::processConnect))

        if (!initialized) {
            login_progress.visibility = View.VISIBLE
            Connection.connect("Connected to server!")
        }

        val viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        viewModel.loggedInLiveData.observe(this, Observer(this::logIn))


        login_button.setOnClickListener({
            login_progress.visibility = View.VISIBLE
            viewModel.login(username.text.toString(), password.text.toString())
        })
    }

    private fun processConnect(connected: Boolean?) {
        if (connected != null) {
            if (connected) {
                if (!initialized) {
                    sendHandshake()
                    initialized = true
                } else {
                    login_progress.visibility = View.GONE
                }
            } else
                login_progress.visibility = View.GONE
            connectData.value = null
        }
    }

    private fun displayConnectionData(message: String?) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            connectionData.value = null
        }
    }

    private fun displayError(message: String?) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            errorData.value = null
        }
    }


    private fun logIn(loggedIn: Boolean?) {
        if (loggedIn == true) {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            finish()
        }
        else
            login_progress.visibility = View.GONE
    }


}
