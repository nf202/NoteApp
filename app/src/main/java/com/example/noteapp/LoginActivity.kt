package com.example.noteapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                Thread {
                    // 向服务器发送请求，验证用户名和密码
                    val response = sendPostRequest(
                        "http://127.0.0.1:8000/user/login/",
                        "username=$username&password=$password"
                    )
                    // 在这里处理服务器的响应
                    runOnUiThread {
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("服务器响应")
                            .setMessage(response)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    if (response.contains("success")) {
                        // 如果登录成功，跳转到MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("username", username)
                        startActivity(intent)
                        finish() //调用finish()方法来结束当前Activity
                    }
                }.start()
            }
        }

        registerButton.setOnClickListener {
            //逻辑不对，理论上应该进入一个新的页面，或者将本页面的一些元素进行一下修改
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
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