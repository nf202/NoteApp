package com.example.noteapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class ChangePassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pass)

        val oldPasswordEditText = findViewById<EditText>(R.id.oldPasswordEditText)
        val newPassword1EditText = findViewById<EditText>(R.id.newPassword1EditText)
        val newPassword2EditText = findViewById<EditText>(R.id.newPassword2EditText)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val username = intent.getStringExtra("username")
        changePasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword1 = newPassword1EditText.text.toString()
            val newPassword2 = newPassword2EditText.text.toString()
            if (oldPassword.isEmpty() || newPassword1.isEmpty() || newPassword2.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            else if(newPassword1 != newPassword2){
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else {
                val client = OkHttpClient()

                val requestBody = FormBody.Builder()
                    .add("old_password", oldPassword)
                    .add("new_password", newPassword1)
                    .add("username", username.toString())
                    .build()

                val request = Request.Builder()
                    .url("http://127.0.0.1:8000/user/change_password/")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        runOnUiThread {
                            if (response.isSuccessful) {
                                Toast.makeText(this@ChangePassActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@ChangePassActivity, "Failed to change password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }

        }
    }
}