package pl.edu.pw.elka.llepak.tinbox

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_main_menu.*
import pl.edu.pw.elka.llepak.tinbox.Connection.connectionData
import pl.edu.pw.elka.llepak.tinbox.Connection.errorData
import pl.edu.pw.elka.llepak.tinbox.tasks.SendFileTask
import pl.edu.pw.elka.llepak.tinbox.viewmodels.MainMenuViewModel
import java.io.File
import java.net.URI

class MainMenuActivity: AppCompatActivity() {

    companion object {
        const val GET_FILE = 37
    }

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
            intent.putExtra(ListFilesActivity.GET_PATH, false)
            startActivity(intent)
            main_menu_progress.visibility = View.GONE
        })

        menu_change_password.setOnClickListener({
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        })

        upload_file_button.setOnClickListener({
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            // special intent for Samsung file manager
            val sIntent = Intent("com.sec.android.app.myfiles.PICK_DATA")
            sIntent.addCategory(Intent.CATEGORY_DEFAULT)

            val chooserIntent: Intent
            if (packageManager.resolveActivity(sIntent, 0) != null) {
                // it is device with samsung file manager
                chooserIntent = Intent.createChooser(sIntent, "Open file")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intent))
            }
            else {
                chooserIntent = Intent.createChooser(intent, "Open file")
            }

            try {
                startActivityForResult(chooserIntent, GET_FILE)
            } catch (ex: android.content.ActivityNotFoundException) {
                Toast.makeText(applicationContext, "No suitable File Manager was found.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GET_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                val newIntent = Intent(this, ListFilesActivity::class.java)
                newIntent.putExtra(ListFilesActivity.FILE, data?.data?.path)
                newIntent.putExtra(ListFilesActivity.GET_PATH, true)
                newIntent.putExtra(ListFilesActivity.PATH, "/")
                startActivity(newIntent)
            }
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