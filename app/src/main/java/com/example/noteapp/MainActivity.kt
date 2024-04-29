package com.example.noteapp

import android.app.Activity
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


class MainActivity : AppCompatActivity() {
    private lateinit var addButton: FloatingActionButton
    companion object {
        private const val EDIT_NOTE_REQUEST = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addButton = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
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

            // 获取notesLayout并将note_item视图添加到notesLayout中
            val notesLayout = findViewById<LinearLayout>(R.id.notesLayout)
            notesLayout.addView(noteView)
        }
    }
}