package com.example.noteapp

import android.content.Intent
import android.graphics.Bitmap
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


class EditActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val addImageButton = findViewById<Button>(R.id.addImageButton)
        val addAudioButton = findViewById<Button>(R.id.addAudioButton)
        val formatTextButton = findViewById<Button>(R.id.formatTextButton)
        val saveButton = findViewById<Button>(R.id.saveButton)

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