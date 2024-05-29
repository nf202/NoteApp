package com.example.noteapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FilterActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var notesScrollView: ScrollView
    private lateinit var username: String

    companion object {
        private const val VIEW_NOTE_REQUEST = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        searchEditText = findViewById(R.id.searchEditText)
        notesScrollView = findViewById(R.id.notesScrollView)

        val categories = arrayOf("备忘", "旅游", "学习") // 类别列表
        categorySpinner = findViewById(R.id.categorySpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        username = intent.getStringExtra("username").toString()

        searchEditText.addTextChangedListener {
            updateNotes()
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateNotes()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun updateNotes() {
        val notesLayout = findViewById<LinearLayout>(R.id.notesLayout)
        notesLayout.removeAllViews()
        val searchText = searchEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        // 创建OkHttpClient
        val client = OkHttpClient()
        // 创建请求体
        val jsonObject = JSONObject()
        jsonObject.put("username", username)
        jsonObject.put("search_text", searchText)
        jsonObject.put("category", category)
        val json = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(json, jsonObject.toString())

        // 创建请求
        val request = Request.Builder()
            .url("http://10.0.2.2:8000/note/filter_note/")
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
                    val category = noteJsonObject.getString("category")

                    // 为新的笔记创建一个note_item视图
                    val noteView = LayoutInflater.from(this@FilterActivity).inflate(R.layout.note_item, null, false)

                    // 设置标题，时间，和简介
                    val titleTextView = noteView.findViewById<TextView>(R.id.noteTitle)
                    val timeTextView = noteView.findViewById<TextView>(R.id.noteTime)
                    val summaryTextView = noteView.findViewById<TextView>(R.id.noteSummary)
                    val categoryTextView = noteView.findViewById<TextView>(R.id.noteCategory)

                    titleTextView.text = title
                    timeTextView.text = time
                    summaryTextView.text = summary
                    categoryTextView.text = category
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
                                val intent = Intent(this@FilterActivity, EditActivity::class.java)
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
//                        val notesLayout = findViewById<LinearLayout>(R.id.notesLayout)
                        notesLayout.addView(noteView)
                    }
                }
            }
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIEW_NOTE_REQUEST && resultCode == Activity.RESULT_OK && data != null){
            setResult(RESULT_OK, data)
            finish()
        }
    }
}