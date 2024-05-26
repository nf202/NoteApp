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
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var addButton: FloatingActionButton
    private var username: String? = null
    companion object {
        private const val EDIT_NOTE_REQUEST = 1
        private const val VIEW_NOTE_REQUEST = 2
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
            intent.putExtra("mode", "add")
            startActivityForResult(intent, EDIT_NOTE_REQUEST)
        }
        // 创建OkHttpClient
        val client = OkHttpClient()
        // 创建请求体
        val jsonObject = JSONObject()
        jsonObject.put("username", username)
        val json = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(json, jsonObject.toString())

        // 创建请求
        val request = Request.Builder()
            .url("http://10.0.2.2:8000/note/get_all_notes/")
            .post(body)
            .build()

        // 发送请求并处理响应
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }

                // 获取响应体
                val responseBody = response.body?.string()

                // 将响应体转换为JSONArray
                val notesJsonArray = JSONArray(responseBody)

                // 遍历JSONArray
                for (i in 0 until notesJsonArray.length()) {
                    val noteJsonObject = notesJsonArray.getJSONObject(i)

                    // 获取笔记的基本信息
                    val title = noteJsonObject.getString("title")
                    val summary = noteJsonObject.getString("summary")
                    val time = noteJsonObject.getString("time")

                    // 为新的笔记创建一个note_item视图
                    val noteView = LayoutInflater.from(this@MainActivity).inflate(R.layout.note_item, null, false)

                    // 设置标题，时间，和简介
                    val titleTextView = noteView.findViewById<TextView>(R.id.noteTitle)
                    val timeTextView = noteView.findViewById<TextView>(R.id.noteTime)
                    val summaryTextView = noteView.findViewById<TextView>(R.id.noteSummary)

                    titleTextView.text = title
                    timeTextView.text = time
                    summaryTextView.text = summary
                    // 设置点击事件
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
                                intent.putExtra("time", time_stamp.toString())
                                intent.putExtra("mode", "view")
                                startActivityForResult(intent, VIEW_NOTE_REQUEST)
                            }
                        })
                    }
                    // 获取notesLayout并将note_item视图添加到notesLayout中
                    runOnUiThread {
                        val notesLayout = findViewById<LinearLayout>(R.id.notesLayout)
                        notesLayout.addView(noteView)
                    }
                }
            }
        })

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_NOTE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // 获取返回的结果
            val title = data.getStringExtra("title")
            val category = data.getStringExtra("category")
            val summary = data.getStringExtra("summary")
            val time = data.getStringExtra("time")

            // 为新的笔记创建一个note_item视图
            val noteView = LayoutInflater.from(this).inflate(R.layout.note_item, null, false)

            // 设置标题，时间，和简介
            val titleTextView = noteView.findViewById<TextView>(R.id.noteTitle)
            val categoryTextView = noteView.findViewById<TextView>(R.id.noteCategory)
            val timeTextView = noteView.findViewById<TextView>(R.id.noteTime)
            val summaryTextView = noteView.findViewById<TextView>(R.id.noteSummary)

            titleTextView.text = title
            categoryTextView.text = category
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
                        intent.putExtra("category", category)
                        intent.putExtra("note", note)
                        intent.putExtra("images", imagesJsonArray.toString())
                        intent.putExtra("username", username)
                        intent.putExtra("time", time_stamp.toString())
                        intent.putExtra("mode", "view")
                        startActivityForResult(intent, VIEW_NOTE_REQUEST)
                    }
                })
            }

            // 获取notesLayout并将note_item视图添加到notesLayout中
            val notesLayout = findViewById<LinearLayout>(R.id.notesLayout)
            notesLayout.addView(noteView)
        }
        if (requestCode == VIEW_NOTE_REQUEST && resultCode == Activity.RESULT_OK && data != null){
            // 获取返回的结果
            val title = data.getStringExtra("title")
            val category = data.getStringExtra("category")
            val summary = data.getStringExtra("summary")
            val time = data.getStringExtra("time")
            val old_time = data.getStringExtra("old_time")

            // 获取notesLayout
            val notesLayout = findViewById<LinearLayout>(R.id.notesLayout)

            // 遍历notesLayout中的所有note_item视图
            for (i in 0 until notesLayout.childCount) {
                val noteView = notesLayout.getChildAt(i)

                // 获取note_item视图中的时间TextView
                val timeTextView = noteView.findViewById<TextView>(R.id.noteTime)

                // 如果时间TextView的文本与old_time相同，那么这就是我们要找的note_item视图
                if (timeTextView.text == old_time) {
                    // 获取标题，时间，和简介TextView
                    val titleTextView = noteView.findViewById<TextView>(R.id.noteTitle)
                    val summaryTextView = noteView.findViewById<TextView>(R.id.noteSummary)
                    val categoryTextView = noteView.findViewById<TextView>(R.id.noteCategory)

                    // 用新的信息替换旧的信息
                    titleTextView.text = title
                    categoryTextView.text = category
                    timeTextView.text = time
                    summaryTextView.text = summary

                    // 找到了就跳出循环
                    break
                }
            }
        }
    }
}