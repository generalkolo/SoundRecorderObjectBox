package com.semanientreprise.soundrecorderbox.utils

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.semanientreprise.soundrecorderbox.R
import com.semanientreprise.soundrecorderbox.models.Recordings
import io.objectbox.Box
import java.io.File
import java.io.IOException

/**
 * Created by Edet Ebenezer on 08/13/2018.
 */
class RecordingService : Service() {
    private var mFileName: String? = null
    private var mFilePath: String? = null
    private lateinit var mRecorder: MediaRecorder
    private var mStartingTimeMillis: Long = 0
    private var mElapsedMillis: Long = 0
    private var recordingsBox: Box<Recordings>? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        recordingsBox = ObjectBox.boxStore.boxFor(Recordings::class.java)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startRecording()
        return START_STICKY
    }

    override fun onDestroy() {
        if (mRecorder != null) {
            stopRecording()
        }
        super.onDestroy()
    }

    private fun startRecording() {
        setFileNameAndPath()
        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(mFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(1)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(192000)
        }

        try {
            mRecorder.prepare()
            mRecorder.start()
            mStartingTimeMillis = System.currentTimeMillis()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
    }

    private fun setFileNameAndPath() {
        var count = 0
        var f: File
        do {
            count++
            mFileName = "${getString(R.string.default_file_name)}_${recordingsBox!!.all.size + count}.mp4"
            mFilePath = "${Environment.getExternalStorageDirectory().absolutePath}/Soundbox/$mFileName"
            f = File(mFilePath!!)
        } while (f.exists() && !f.isDirectory)
    }

    private fun stopRecording() {
        mRecorder.stop()
        mElapsedMillis = System.currentTimeMillis() - mStartingTimeMillis
        mRecorder.release()
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show()
        try {
            val recordings = Recordings(0, mFileName, mFilePath, mElapsedMillis)
            recordingsBox!!.put(recordings)
            Toast.makeText(this, "New Recording Added", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "exception", e)
        }
    }

    companion object {
        private const val LOG_TAG = "RecordingService"
    }
}