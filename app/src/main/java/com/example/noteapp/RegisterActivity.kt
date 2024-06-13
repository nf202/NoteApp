package com.example.noteapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var password1EditText: EditText
    private lateinit var password2EditText: EditText
    private lateinit var telephoneEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        password1EditText = findViewById(R.id.password1EditText)
        password2EditText = findViewById(R.id.password2EditText)
        telephoneEditText = findViewById(R.id.telephoneEditText)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password1 = password1EditText.text.toString()
            val password2 = password2EditText.text.toString()
            val telephone = telephoneEditText.text.toString()

            if (username.isNotEmpty() && password1.isNotEmpty() && password2.isNotEmpty() && telephone.isNotEmpty()) {
                if (password1 == password2) {
                    register(username, password1, telephone)
                } else {
                    Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun register(username: String, password: String, telephone: String) {
        Thread {
            // 向服务器发送请求，验证用户名和密码
            val response = sendPostRequest(
                "http://10.0.2.2:8000/user/register/",
                "username=$username&password=$password&telephone=$telephone"
            )
            // 在这里处理服务器的响应
            runOnUiThread {
                AlertDialog.Builder(this@RegisterActivity)
                    .setTitle("服务器响应")
                    .setMessage(response)
                    .setPositiveButton("OK", null)
                    .show()
            }
            if (response.contains("success")) {
                finish() //调用finish()方法来结束当前Activity
            }
        }.start()
    }
    private fun sendPostRequest(urlString: String, postData: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.doOutput = true

        val outputStream = DataOutputStream(conn.outputStream)
        outputStream.writeBytes(postData)
        outputStream.flush()
        outputStream.close()

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = conn.inputStream
            return inputStream.bufferedReader().use { it.readText() }
        } else {
            return "POST request not worked"
        }
    }
}