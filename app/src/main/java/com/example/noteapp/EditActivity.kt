package com.example.noteapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.style.ClickableSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.noteapp.ui.theme.AudioView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import java.io.File


// 定义音频状态常量
private val AUDIO_PLAY_NONE = 0
private val AUDIO_PLAY_ING = 1

fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}
class EditActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private val PICK_AUDIO_REQUEST = 2
    private val CAPTURE_IMAGE_REQUEST = 3
    private val RECORD_AUDIO_REQUEST = 4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val addImageButton = findViewById<Button>(R.id.addImageButton)
        val addAudioButton = findViewById<Button>(R.id.addAudioButton)
        val formatTextButton = findViewById<Button>(R.id.formatTextButton)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val abstract_button = findViewById<FloatingActionButton>(R.id.abstractButton)
        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        val noteEditText = findViewById<EditText>(R.id.noteEditText)

        val categories = arrayOf("备忘", "旅游", "学习") // 类别列表
        val spinner: Spinner = findViewById(R.id.categorySpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val username = intent.getStringExtra("username")
        val mode = intent.getStringExtra("mode")
        var time: String? = null
        // 获取Intent中的额外数据
        if (intent.hasExtra("title")) {
            val title = intent.getStringExtra("title")
            val category = intent.getStringExtra("category")
            val note = intent.getStringExtra("note")
            val imagesJsonString = intent.getStringExtra("images")
            time = intent.getStringExtra("time")
            // 将title和note显示在对应的控件中
            titleEditText.setText(title)
            noteEditText.setText(note)
            spinner.setSelection(categories.indexOf(category))

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
        abstract_button.setOnClickListener {
            val note = noteEditText.text.toString()
            val jsonObject = JSONObject()
            jsonObject.put("note", note)
            val json = "application/json; charset=utf-8".toMediaType()
            val body = RequestBody.create(json, jsonObject.toString())
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url("http://10.0.2.2:8000/note/wenxin_note/")
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
                    // 处理响应,获取摘要,返回的response为一个文本
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        AlertDialog.Builder(this@EditActivity)
                            .setTitle("服务器响应")
                            .setMessage(responseBody)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            })
        }
        addImageButton.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            // 启动图片选择器
//            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            // 创建一个AlertDialog.Builder对象
            val builder = AlertDialog.Builder(this)
            builder.setTitle("选择图片来源")
            builder.setItems(arrayOf("从文件中选择", "拍摄")) { _, which ->
                when (which) {
                    0 -> {
                        // 从文件中选择
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, PICK_IMAGE_REQUEST)
                    }
                    1 -> {
                        // 直接拍摄
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
                    }
                }
            }
            builder.show()
        }

        addAudioButton.setOnClickListener {
            // Todo: 实现录音功能，下面代码存在bug，无法成功处理Intent
            // 创建一个AlertDialog.Builder对象
            val builder = AlertDialog.Builder(this)
            builder.setTitle("选择音频来源")
            builder.setItems(arrayOf("从文件中选择", "直接录音")) { _, which ->
                when (which) {
                    0 -> {
                        // 从文件中选择
                        Log.d("EditActivity", "Coming here11")
                        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, PICK_AUDIO_REQUEST)

                    }
                    1 -> {
                        // 直接录音
                        val intent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                        startActivityForResult(intent, RECORD_AUDIO_REQUEST)
                    }
                }
            }
            builder.show()

        }

//        formatTextButton.setOnClickListener {
//            // 在这里编写格式化文本的代码
//            // 有空再说
//        }

        saveButton.setOnClickListener {
            // 在这里编写保存的代码
            val title = titleEditText.text.toString()
            val category = spinner.selectedItem.toString()
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
                if(mode == "add") {
                    val jsonObject = JSONObject()
                    jsonObject.put("username", username)
                    jsonObject.put("title", title)
                    jsonObject.put("category", category)
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
                                    putExtra("category", category)
                                }
                                setResult(RESULT_OK, data)
                                finish()
                            }
                        }
                    })
                } else {
                    val jsonObject = JSONObject()
                    jsonObject.put("username", username)
                    jsonObject.put("title", title)
                    jsonObject.put("category", category)
                    jsonObject.put("note", note)
                    jsonObject.put("summary", summary)
                    jsonObject.put("time", currentTime)
                    jsonObject.put("old_time", time)
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
                        .url("http://10.0.2.2:8000/note/change_note/")
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
                                    putExtra("category", category)
                                    putExtra("old_time", time)
                                }
                                setResult(RESULT_OK, data)
                                finish()
                            }
                        }
                    })

                }

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
        Log.d("EditActivity", resultCode.toString())
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

        } else if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // 处理拍摄的图片
            val bitmap = data.extras?.get("data") as Bitmap
            // 获取EditText的引用
            val noteEditText = findViewById<EditText>(R.id.noteEditText)

            // 获取EditText的宽度
            val editTextWidth = noteEditText.width

            // 调整图片的大小
            val imageWidth = editTextWidth
            val imageHeight = bitmap.height * imageWidth / bitmap.width
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, true)

            // 创建一个ImageSpan
            val imageSpan = ImageSpan(this, scaledBitmap)

            // 创建一个SpannableString
            val spannableString = SpannableString(" ")
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // 获取EditText的现有文本
            val existingText = noteEditText.text

            // 创建一个新的SpannableStringBuilder，并将现有的文本和新的SpannableString添加到其中
            val spannableStringBuilder = SpannableStringBuilder(existingText).append(spannableString)

            // 将新的SpannableStringBuilder设置为EditText的文本
            noteEditText.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE)
        } else if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            // Todo: 处理选择音频文件的Intent结果
            // 获取用户选择的音频文件的Uri
            val selectedAudioUri = data.data
            // 创建一个AudioView
            val audioView = AudioView(this)
            // 设置音频文件的Uri
            if (selectedAudioUri != null) {
                audioView.setAudioUri(selectedAudioUri)
            }
            // 放在当前时间布局的最后
            val audioLayout = findViewById<LinearLayout>(R.id.audioLayout)
            // 往其中加入audioView对象:
            audioLayout.addView(audioView)
        } else if (requestCode == RECORD_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            // Todo: 处理录音Intent的结果
            val selectedAudioUri = data.data
            // 创建一个AudioView
            val audioView = AudioView(this)
            // 设置音频文件的Uri
            if (selectedAudioUri != null) {
                audioView.setAudioUri(selectedAudioUri)
            }
            // 放在当前时间布局的最后
            val audioLayout = findViewById<LinearLayout>(R.id.audioLayout)
            // 往其中加入audioView对象:
            audioLayout.addView(audioView)
        }
    }
}