package com.example.noteapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Base64
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient


fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}
class EditActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val addImageButton = findViewById<Button>(R.id.addImageButton)
        val addAudioButton = findViewById<Button>(R.id.addAudioButton)
        val formatTextButton = findViewById<Button>(R.id.formatTextButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        val noteEditText = findViewById<EditText>(R.id.noteEditText)
        val username = intent.getStringExtra("username")
        print(username)
        // 获取Intent中的额外数据
        if (intent.hasExtra("title")) {
            val title = intent.getStringExtra("title")
            val note = intent.getStringExtra("note")
            val imagesJsonString = intent.getStringExtra("images")

            // 将title和note显示在对应的控件中
            titleEditText.setText(title)
            noteEditText.setText(note)

            // 将imagesJsonString转换为JSONArray
            val imagesJsonArray = JSONArray(imagesJsonString)

            // 遍历JSONArray
            for (i in 0 until imagesJsonArray.length()) {
                val imageJsonObject = imagesJsonArray.getJSONObject(i)
                val start = imageJsonObject.getInt("start")
                val base64 = imageJsonObject.getString("base64")

                // 将base64字符串转换为Bitmap
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                // 创建一个ImageSpan
                val imageSpan = ImageSpan(this, bitmap)

                // 获取noteEditText的现有文本
                val spannableString = SpannableString(noteEditText.text)

                // 在对应的位置插入ImageSpan
                spannableString.setSpan(
                    imageSpan,
                    start,
                    start + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // 将新的SpannableString设置为noteEditText的文本
                noteEditText.setText(spannableString, TextView.BufferType.SPANNABLE)
            }
        }
        addImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // 启动图片选择器
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        addAudioButton.setOnClickListener {
            // 在这里编写添加音频的代码
        }

        formatTextButton.setOnClickListener {
            // 在这里编写格式化文本的代码
        }

        saveButton.setOnClickListener {
            // 在这里编写保存的代码
            val title = titleEditText.text.toString()
            val note = noteEditText.text.toString()
            val summary = if (note.length > 10) note.substring(0, 10) else note
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date()
            )
            // 获取所有的ImageSpan
            val imageSpans = noteEditText.text.getSpans(0, noteEditText.text.length, ImageSpan::class.java)
            val images = mutableListOf<Pair<Int, String>>()
            for (imageSpan in imageSpans) {
                val bitmap = (imageSpan.drawable as BitmapDrawable).bitmap
                val base64 = bitmapToBase64(bitmap)
                val start = noteEditText.text.getSpanStart(imageSpan)
                images.add(Pair(start, base64))
            }
            Thread {
                // 构建你的 JSON 数据
                val jsonObject = JSONObject()
                jsonObject.put("username", username)
                jsonObject.put("title", title)
                jsonObject.put("note", note)
                jsonObject.put("summary", summary)
                jsonObject.put("time", currentTime)
                val imagesJsonArray = JSONArray()
                for (image in images) {
                    val imageJsonObject = JSONObject()
                    imageJsonObject.put("start", image.first)
                    imageJsonObject.put("base64", image.second)
                    imagesJsonArray.put(imageJsonObject)
                }
                jsonObject.put("images", imagesJsonArray)
                val json = "application/json; charset=utf-8".toMediaType()
                val body = RequestBody.create(json, jsonObject.toString())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8000/note/store_note/")
                    .post(body)
                    .build()
                val client = OkHttpClient()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                    }
                    override fun onResponse(call: Call, response: Response) {
                        if (!response.isSuccessful) {
                            throw IOException("Unexpected code $response")
                        }
                        val responseBody = response.body?.string()
                        // 处理响应
                        runOnUiThread {
                            AlertDialog.Builder(this@EditActivity)
                                .setTitle("服务器响应")
                                .setMessage(responseBody)
                                .setPositiveButton("OK", null)
                                .show()
                        }
                        if (responseBody?.contains("success") == true) {
                            // 如果保存成功，结束当前Activity
                            // finish()
                            val data = Intent().apply {
                                putExtra("title", title)
                                putExtra("summary", summary)
                                putExtra("time", currentTime)
                            }
                            setResult(RESULT_OK, data)
                            finish()
                        }
                    }
                })
            }.start()

        }
        titleEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // 当标题EditText获得焦点时，使插入图片和音频的按钮失效
                addImageButton.isEnabled = false
                addAudioButton.isEnabled = false
            } else {
                // 当标题EditText失去焦点时，使插入图片和音频的按钮有效
                addImageButton.isEnabled = true
                addAudioButton.isEnabled = true
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 检查是否是我们发起的图片选择请求
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // 获取用户选择的图片的Uri
            val selectedImageUri = data.data
            // 将Uri转换为Bitmap
            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)

            // 获取EditText的引用
            val noteEditText = findViewById<EditText>(R.id.noteEditText)

            // 获取EditText的宽度
            val editTextWidth = noteEditText.width

            // 调整图片的大小
            val imageWidth = editTextWidth
            val imageHeight = bitmap.height * imageWidth / bitmap.width
            bitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, true)

            // 创建一个ImageSpan
            val imageSpan = ImageSpan(this, bitmap)

            // 创建一个SpannableString
            val spannableString = SpannableString(" ")
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // 获取EditText的现有文本
            val existingText = noteEditText.text

            // 创建一个新的SpannableStringBuilder，并将现有的文本和新的SpannableString添加到其中
            val spannableStringBuilder = SpannableStringBuilder(existingText).append(spannableString)

            // 将新的SpannableStringBuilder设置为EditText的文本
            noteEditText.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE)

        }
    }
}