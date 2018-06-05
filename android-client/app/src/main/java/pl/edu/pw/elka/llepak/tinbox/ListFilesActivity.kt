package pl.edu.pw.elka.llepak.tinbox

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_list_files.*
import pl.edu.pw.elka.llepak.tinbox.R.layout.activity_list_files
import pl.edu.pw.elka.llepak.tinbox.protobuf.File
import pl.edu.pw.elka.llepak.tinbox.protobuf.FileType
import pl.edu.pw.elka.llepak.tinbox.viewmodels.ListFilesViewModel

class ListFilesActivity: AppCompatActivity() {

    companion object {
        const val PATH = "path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_files)

        val viewModel = ViewModelProviders.of(this).get(ListFilesViewModel::class.java)

        Connection.connectionData.observe(this, Observer(this::displayConnectionData))
        Connection.errorData.observe(this, Observer(this::displayError))

        viewModel.listFilesLiveData.observe(this, Observer(this::listFiles))

        Thread({
            viewModel.listFiles(intent.getStringExtra(PATH))
        }).start()

        create_folder_button.setOnClickListener({
            val editText = EditText(this)
            editText.hint = "folder name"
            val alertBuilder = AlertDialog.Builder(this)
            alertBuilder.setMessage("New folder name")
                    .setView(editText)
            alertBuilder.setPositiveButton("Create", {_, _ ->
                val path = StringBuilder().append(intent.getStringExtra(PATH))
                if (!intent.getStringExtra(PATH).endsWith("/"))
                    path.append("/")
                path.append(editText.text.toString())
                Thread({
                    viewModel.mkdir(path.toString())
                    val newIntent = Intent(this, ListFilesActivity::class.java)
                    newIntent.putExtra(PATH, intent.getStringExtra(PATH))
                    startActivity(newIntent)
                    finish()
                }).start()
            }).show()
        })
    }

    private fun listFiles(files: List<File>?) {
        if (files == null)
            Toast.makeText(this, "Cannot get user files", Toast.LENGTH_SHORT).show()
        else {
            for (file in files) {
                val textView = TextView(this)
                textView.text = file.filename
                textView.setOnClickListener({
                    if (file.filetype == FileType.DIRECTORY)
                        goIntoDirectory(file.filename)
                    else
                        showFileInfo(file)
                })
                list_files_layout.addView(textView)
            }
        }
    }

    private fun showFileInfo(file: File) {
        val message = StringBuilder()
        message.append("filename: ").append(file.filename).append("\n")
        message.append("size: ").append(file.size).append("\n")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("File info")
                .setMessage(message.toString())
                .show()
    }

    private fun goIntoDirectory(path: String) {
        val newIntent = Intent(this, ListFilesActivity::class.java)
        newIntent.putExtra(PATH, path)
        startActivity(newIntent)
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