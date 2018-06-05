package pl.edu.pw.elka.llepak.tinbox

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main_menu.*
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.Connection.errorData
import pl.edu.pw.elka.llepak.tinbox.protobuf.File
import pl.edu.pw.elka.llepak.tinbox.tasks.ListFilesTask
import pl.edu.pw.elka.llepak.tinbox.viewmodels.MainMenuViewModel

class MainMenuActivity: AppCompatActivity() {

    lateinit var viewModel: MainMenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        connectionData.observe(this, Observer(this::displayConnectionData))
        errorData.observe(this, Observer(this::displayError))

        viewModel = ViewModelProviders.of(this).get(MainMenuViewModel::class.java)

        viewModel.loggedOutLiveData.observe(this, Observer(this::logOut))

        main_menu_progress.visibility = View.GONE

        logout_button.setOnClickListener({
            main_menu_progress.visibility = View.VISIBLE
            viewModel.logout()
        })

        file_list_button.setOnClickListener({
            main_menu_progress.visibility = View.VISIBLE
            val intent = Intent(this, ListFilesActivity::class.java)
            intent.putExtra(ListFilesActivity.PATH, "/")
            startActivity(intent)
            main_menu_progress.visibility = View.GONE
        })

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