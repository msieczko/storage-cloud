package pl.edu.pw.elka.llepak.tinbox

import android.app.ProgressDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_list_files.*
import pl.edu.pw.elka.llepak.tinbox.protobuf.File
import pl.edu.pw.elka.llepak.tinbox.protobuf.FileType
import pl.edu.pw.elka.llepak.tinbox.viewmodels.ListFilesViewModel
import java.util.*

class ListFilesActivity: AppCompatActivity() {

    companion object {
        const val PATH = "path"
        const val GET_PATH = "get_path"
        const val FILE = "FILE"
    }

    lateinit var progressDialog: ProgressDialog

    lateinit var viewModel: ListFilesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_files)

        viewModel = ViewModelProviders.of(this).get(ListFilesViewModel::class.java)

        Connection.connectionData.observe(this, Observer(this::displayConnectionData))
        Connection.errorData.observe(this, Observer(this::displayError))
        Connection.transferProgressData.observe(this, Observer(this::updateProgress))

        viewModel.listFilesLiveData.observe(this, Observer(this::listFiles))
        viewModel.fileTransferLiveData.observe(this, Observer(this::checkTransfer))
        viewModel.mkdirLiveData.observe(this, Observer(this::reloadActivity))
        viewModel.deletedLiveData.observe(this, Observer(this::refreshAfterDelete))


        Thread({
            viewModel.listFiles(intent.getStringExtra(PATH))
        }).start()

        if (intent.getBooleanExtra(GET_PATH, false)) {
            create_folder_button.visibility = View.GONE
            delete_button.visibility = View.GONE
            choose_dir_button.visibility = View.VISIBLE
        }
        else {
            create_folder_button.visibility = View.VISIBLE
            delete_button.visibility = View.VISIBLE
            choose_dir_button.visibility = View.GONE
        }

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
                }).start()
            }).show()
        })

        choose_dir_button.setOnClickListener({
            viewModel.uploadFile(intent.getStringExtra(FILE), intent.getStringExtra(PATH))
            displayProgressDialog("Sending data")
        })

        delete_button.setOnClickListener({
            val path = intent.getStringExtra(PATH)
            val changedPath = path.replaceAfterLast("/", "").removeSuffix("/")
            intent.putExtra(PATH, changedPath)
            viewModel.delete(path)
        })

        progressDialog = ProgressDialog(this)
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
        val filename = file.filename.replaceBeforeLast("/", "", "").replace("/", "")
        message.append("filename: ").append(filename).append("\n")
        message.append("size: ").append(file.size).append(" bytes").append("\n")
        message.append("creation date: ").append(Date(file.creationDate).toString()).append("\n")
        message.append("owner: ").append(file.owner).append("\n")
        message.append("is shared: ").append(file.isShared).append("\n")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("File info")
                .setMessage(message.toString())
                .setNegativeButton("Delete", { _, _ ->
                    viewModel.delete(file.filename)
                })
                .setPositiveButton("Download", {_, _ ->
                    viewModel.download(file)
                    displayProgressDialog("Downloading data")
                })
                .show()
    }

    private fun goIntoDirectory(path: String) {
        val newIntent = Intent(this, ListFilesActivity::class.java)
        newIntent.putExtra(PATH, path)
        newIntent.putExtra(FILE, intent.getStringExtra(FILE))
        newIntent.putExtra(GET_PATH, intent.getBooleanExtra(GET_PATH, false))
        startActivity(newIntent)
        finish()
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

    private fun updateProgress(value: Int?) {
        if (value != null) {
            progressDialog.progress = value
        }
    }

    private fun reloadActivity(created: Boolean?) {
        if (created == true) {
            goIntoDirectory(intent.getStringExtra(PATH))
            finish()
        }
    }

    private fun checkTransfer(transferred: Boolean?) {
        progressDialog.cancel()
        if (transferred == true) {
            goIntoDirectory(intent.getStringExtra(PATH))
            finish()
        }
    }

    private fun refreshAfterDelete(deleted: Boolean?) {
        if (deleted == true) {
            goIntoDirectory(intent.getStringExtra(PATH))
            finish()
        }
    }

    private fun displayProgressDialog(title: String) {
        progressDialog.setTitle(title)
        progressDialog.setCancelable(false)
        progressDialog.max = 100
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.progress = 0
        progressDialog.setProgressNumberFormat(null)
        progressDialog.show()
    }
}