package com.example.noteapp.ui.theme

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.noteapp.R
import java.util.Timer
import java.util.TimerTask

class AudioView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var playPauseImageView: ImageView
    private var audioTimeTextView: TextView
    private var mediaPlayer: MediaPlayer? = null
    private var timer: Timer? = null
    private var isPlaying: Boolean = false
    private var audioUri : Uri? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.audio_view, this, true)
        playPauseImageView = findViewById(R.id.playPauseImageView)
        audioTimeTextView = findViewById(R.id.audioTimeTextView)

        playPauseImageView.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            } else {
                playAudio()
            }
        }
    }

    fun setAudioResource(audioResId: Int) {
        mediaPlayer = MediaPlayer.create(context, audioResId)
        mediaPlayer?.setOnCompletionListener {
            resetAudio()
        }
    }

    fun setAudioUri(audioUri: Uri) {
        this.audioUri = audioUri
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, audioUri)
            prepare()
            setOnCompletionListener {
                resetAudio()
            }
        }
    }

    fun getAudioUri() : Uri? {
        return audioUri
    }

    private fun playAudio() {
        mediaPlayer?.start()
        isPlaying = true
        playPauseImageView.setImageResource(R.drawable.ic_play)
        startTimer()
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        playPauseImageView.setImageResource(R.drawable.ic_pause)
        stopTimer()
    }

    private fun resetAudio() {
        mediaPlayer?.seekTo(0)
        isPlaying = false
        playPauseImageView.setImageResource(R.drawable.ic_play)
        stopTimer()
        updateAudioTime()
    }

    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                post {
                    updateAudioTime()
                }
            }
        }, 0, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun updateAudioTime() {
        mediaPlayer?.let {
            val currentPosition = it.currentPosition / 1000
            val minutes = currentPosition / 60
            val seconds = currentPosition % 60
            audioTimeTextView.text = String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mediaPlayer?.release()
        stopTimer()
    }
}
