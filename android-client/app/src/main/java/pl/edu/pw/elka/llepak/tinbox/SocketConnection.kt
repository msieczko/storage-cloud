package pl.edu.pw.elka.llepak.tinbox

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class SocketConnection(private val socket: Socket): AsyncTask<String, Unit, String>() {

    override fun doInBackground(vararg params: String?): String {
        val out = PrintWriter(socket.getOutputStream(), true)
        out.println(params[0] ?: "null")
        val inSock = BufferedReader(
                InputStreamReader(socket.getInputStream()))
        val answer = inSock.readLine()
        return answer
    }

    override fun onPostExecute(result: String?) {
        if (result == null) {
            Log.e("answer", "null")
        }
        else {
            Log.i("answer", result)
        }
    }
}