package pl.edu.pw.elka.llepak.tinbox

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main_menu.*
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.viewmodels.MainMenuViewModel

class MainMenuActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        connectionData.observe(this, Observer(this::displayConnectionData))

        val viewModel = ViewModelProviders.of(this).get(MainMenuViewModel::class.java)

        viewModel.loggedOutLiveData.observe(this, Observer(this::logOut))

        main_menu_progress.visibility = View.GONE

        logout_button.setOnClickListener({
            main_menu_progress.visibility = View.VISIBLE
            viewModel.logout()
        })
    }

    private fun displayConnectionData(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun logOut(loggedOut: Boolean?) {
        if (loggedOut == true) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        else
            main_menu_progress.visibility = View.GONE
    }
}