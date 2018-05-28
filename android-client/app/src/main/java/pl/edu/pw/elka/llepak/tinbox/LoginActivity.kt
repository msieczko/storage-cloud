package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.viewmodels.LoginViewModel

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Connection.initialize()

        connectionData.observe(this, Observer(this::displayConnectionData))

        val viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        viewModel.loggedInLiveData.observe(this, Observer(this::logIn))

        login_progress.visibility = View.GONE

        login_button.setOnClickListener({
            login_progress.visibility = View.VISIBLE
            viewModel.login(username.text.toString(), password.text.toString())
        })
    }



    private fun displayConnectionData(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
