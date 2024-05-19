package com.example.noteapp

import android.app.Activity
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var addButton: FloatingActionButton
    private var username: String? = null
    companion object {
        private const val EDIT_NOTE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        username = intent.getStringExtra("username")
        print(username)
        addButton = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("username", username)
            startActivityForResult(intent, EDIT_NOTE_REQUEST)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_NOTE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // 获取返回的结果
            val title = data.getStringExtra("title")
            val summary = data.getStringExtra("summary")
            val time = data.getStringExtra("time")

            // 为新的笔记创建一个note_item视图
            val noteView = LayoutInflater.from(this).inflate(R.layout.note_item, null, false)

            // 设置标题，时间，和简介
            val titleTextView = noteView.findViewById<TextView>(R.id.noteTitle)
            val timeTextView = noteView.findViewById<TextView>(R.id.noteTime)
            val summaryTextView = noteView.findViewById<TextView>(R.id.noteSummary)

            titleTextView.text = title
            timeTextView.text = time
            summaryTextView.text = summary
            noteView.setOnClickListener{
                val time_stamp = timeTextView.text
                val jsonObject = JSONObject()
                jsonObject.put("time", time_stamp)
                jsonObject.put("username", username)
                val json = "application/json; charset=utf-8".toMediaType()
                val body = RequestBody.create(json, jsonObject.toString())
                val client = OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url("http://10.0.2.2:8000/note/get_note/")
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
                        // 处理响应
                        val responseBody = response.body?.string()
                        val jsonObject = responseBody?.let { it1 -> JSONObject(it1) }
                        val title = jsonObject?.getString("title")
                        val note = jsonObject?.getString("note")
                        val imagesJsonArray = jsonObject?.getJSONArray("images")
                        val intent = Intent(this@MainActivity, EditActivity::class.java)
                        intent.putExtra("title", title)
                        intent.putExtra("note", note)
                        intent.putExtra("images", imagesJsonArray.toString())
                        intent.putExtra("username", username)
                        startActivity(intent)
                    }
                })
            }

            // 获取notesLayout并将note_item视图添加到notesLayout中
            val notesLayout = findViewById<LinearLayout>(R.id.notesLayout)
            notesLayout.addView(noteView)
        }
    }
}