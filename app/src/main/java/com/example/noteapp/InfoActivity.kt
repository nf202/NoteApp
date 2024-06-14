package com.example.noteapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import android.util.Base64
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class InfoActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    private lateinit var profileImageView: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var signatureEditText: EditText
    private lateinit var encodedImage: String
    private lateinit var saveButton: Button
    private var avatar_changed = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        Log.e("InfoActivity", "onCreate")
        profileImageView = findViewById(R.id.avatarImageView)
        usernameEditText = findViewById(R.id.usernameEditText)
        signatureEditText = findViewById(R.id.signatureEditText)
        saveButton = findViewById(R.id.saveButton)

        val old_username = intent.getStringExtra("username")
        // 先根据old_username获取用户信息，然后填充到控件中
        Thread {
            // 向服务器发送请求，根据用户名获取用户信息，填充到控件中
            val response = sendPostRequest(
                "http://127.0.0.1:8000/user/get_info/",
                "username=$old_username"
            )

            // 解析服务器返回的JSON数据
            val jsonResponse = JSONObject(response)
            val username = jsonResponse.getString("username")
            val signature = jsonResponse.getString("signature")
            encodedImage = jsonResponse.getString("avatar")
            Log.e("InfoActivity", "have got the info")
            // 判断是否有头像
            if (encodedImage == "null") {
                Log.e("InfoActivity", "encodedImage is null")
                //将drawable中的default_avatar_icon图片设置到ImageView中
                val defaultAvatar = resources.getDrawable(R.drawable.default_avatar_icon, null)
                runOnUiThread {
                    usernameEditText.setText(username)
                    signatureEditText.setText(signature)
                    profileImageView.setImageDrawable(defaultAvatar)
                }
            }
            else {
                Log.e("InfoActivity", "encodedImage is not null")
                // 将Base64字符串转换为Bitmap
                val decodedString = Base64.decode(encodedImage, Base64.DEFAULT)
                Log.e("InfoActivity", "decodedString")
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                Log.e("InfoActivity", "decodeByte" )
                // 在UI线程中更新控件的内容
                runOnUiThread {
                    usernameEditText.setText(username)
                    signatureEditText.setText(signature)
                    profileImageView.setImageBitmap(decodedByte)
                }
            }
        }.start()
        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        saveButton.setOnClickListener {
            val new_username = usernameEditText.text.toString()
            val signature = signatureEditText.text.toString()

            if (encodedImage != "null" || avatar_changed == 1) {
                // Convert the profile image to a Base64 string
                // 获取ImageView中的Bitmap
                val bitmap = (profileImageView.drawable as BitmapDrawable).bitmap
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
            val jsonObject = JSONObject()
            jsonObject.put("old_username", old_username)
            jsonObject.put("new_username", new_username)
            jsonObject.put("signature", signature)
            jsonObject.put("avatar", encodedImage)

            val json = "application/json; charset=utf-8".toMediaType()
            val body = RequestBody.create(json, jsonObject.toString())
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url("http://127.0.0.1:8000/user/set_info/")
                .post(body)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        if (responseData.contains("success")) {
                            // 把新用户名和头像传回给MainActivity
                            val data = Intent().apply {
                                putExtra("new_username", new_username)
                                putExtra("avatar", encodedImage)
                            }
                            setResult(RESULT_OK, data)
                            finish()
                        }
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            avatar_changed = 1
            Log.d("InfoActivity", "avatar changed")
            val selectedImage: Uri? = data.data
            val imageStream: InputStream? = selectedImage?.let { contentResolver.openInputStream(it) }
            val selectedImageBitmap: Bitmap = BitmapFactory.decodeStream(imageStream)
            profileImageView.setImageBitmap(selectedImageBitmap)
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
}