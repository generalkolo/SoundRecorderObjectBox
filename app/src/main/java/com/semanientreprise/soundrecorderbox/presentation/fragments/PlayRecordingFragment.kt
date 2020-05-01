package com.semanientreprise.soundrecorderbox.presentation.fragments

import android.app.Dialog
import android.graphics.ColorFilter
import android.graphics.LightingColorFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.semanientreprise.soundrecorderbox.R
import com.semanientreprise.soundrecorderbox.databinding.FragmentPlayRecordingBinding
import com.semanientreprise.soundrecorderbox.models.Recordings
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */
class PlayRecordingFragment : DialogFragment() {
    private var _binding: FragmentPlayRecordingBinding? = null
    private val binding get() = _binding!!

    private var recording: Recordings? = null
    private val mHandler = Handler()
    private var mMediaPlayer: MediaPlayer? = null

    //stores whether or not the mediaplayer is currently playing audio
    private var isMediaPlaying = false

    //stores minutes and seconds of the length of the file.
    var minutes: Long = 0
    var seconds: Long = 0
    fun newInstance(item: Recordings?): PlayRecordingFragment {
        val f = PlayRecordingFragment()
        val b = Bundle()
        b.putParcelable(ARG_ITEM, item)
        f.arguments = b
        return f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recording = arguments!!.getParcelable(ARG_ITEM)
        val itemDuration = recording!!.recording_length
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration)
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState).apply {
            // request a window without the title
            window?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        val builder = AlertDialog.Builder(activity!!)
        _binding = FragmentPlayRecordingBinding.inflate(activity!!.layoutInflater)
        val filter: ColorFilter = LightingColorFilter(resources.getColor(R.color.colorPrimary), resources.getColor(R.color.colorPrimary))

        with(binding) {
            seekbar.progressDrawable.colorFilter = filter
            seekbar.thumb.colorFilter = filter
            seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (mMediaPlayer != null && fromUser) {
                        mMediaPlayer!!.seekTo(progress)
                        mHandler.removeCallbacks(mRunnable)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer!!.currentPosition.toLong())
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer!!.currentPosition.toLong()) - TimeUnit.MINUTES.toSeconds(minutes)
                        currentProgress.text = String.format("%02d:%02d", minutes, seconds)
                        updateSeekBar()
                    } else if (mMediaPlayer == null && fromUser) {
                        prepareMediaPlayerFromPoint(progress)
                        updateSeekBar()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    if (mMediaPlayer != null) { // remove message Handler from updating progress bar
                        mHandler.removeCallbacks(mRunnable)
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (mMediaPlayer != null) {
                        mHandler.removeCallbacks(mRunnable)
                        mMediaPlayer!!.seekTo(seekBar.progress)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer!!.currentPosition.toLong())
                        val seconds = (TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer!!.currentPosition.toLong())
                                - TimeUnit.MINUTES.toSeconds(minutes))
                        currentProgress.text = String.format("%02d:%02d", minutes, seconds)
                        updateSeekBar()
                    }
                }
            })
            fileName.text = recording!!.recording_name
            fileLength.text = String.format("%02d:%02d", minutes, seconds)
        }
        initClickListeners()
        builder.setView(binding.root)
        return builder.create()
    }

    private fun initClickListeners() {
        binding.fabPlay.setOnClickListener {
            onPlay(isMediaPlaying)
            isMediaPlaying = !isMediaPlaying
        }
    }

    override fun onStart() {
        super.onStart()
        //set transparent background
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        //disable buttons from dialog
        (dialog as AlertDialog).apply {
            getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
            getButton(Dialog.BUTTON_NEGATIVE).isEnabled = false
            getButton(Dialog.BUTTON_NEUTRAL).isEnabled = false
        }
    }

    // Play start/stop
    private fun onPlay(isPlaying: Boolean) {
        if (!isPlaying) { //currently MediaPlayer is not playing audio
            if (mMediaPlayer == null) {
                startPlaying() //start from beginning
            } else {
                resumePlaying() //resume the currently paused MediaPlayer
            }
        } else { //pause the MediaPlayer
            pausePlaying()
        }
    }

    private fun startPlaying() {
        binding.fabPlay.setImageResource(R.drawable.ic_pause)
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(recording!!.recording_path)
            mMediaPlayer!!.prepare()
            binding.seekbar.max = mMediaPlayer!!.duration
            mMediaPlayer!!.setOnPreparedListener { mMediaPlayer!!.start() }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
        mMediaPlayer!!.setOnCompletionListener { stopPlaying() }
        updateSeekBar()
        //keep screen on while playing audio
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun prepareMediaPlayerFromPoint(progress: Int) { //set mediaPlayer to start from middle of the audio file
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(recording!!.recording_path)
            mMediaPlayer!!.prepare()
            binding.seekbar.max = mMediaPlayer!!.duration
            mMediaPlayer!!.seekTo(progress)
            mMediaPlayer!!.setOnCompletionListener { stopPlaying() }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
        //keep screen on while playing audio
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun pausePlaying() {
        binding.fabPlay.setImageResource(R.drawable.ic_media_play)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer!!.pause()
    }

    private fun resumePlaying() {
        binding.fabPlay.setImageResource(R.drawable.ic_pause)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer!!.start()
        updateSeekBar()
    }

    private fun stopPlaying() {
        binding.fabPlay.setImageResource(R.drawable.ic_media_play)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer?.let {
            it.stop()
            it.reset()
            it.release()
        }
//
//        binding.seekbar.progress = binding.seekbar.max
        isMediaPlaying = !isMediaPlaying
        binding.currentProgress.text = binding.fileLength.text
        binding.seekbar.progress = binding.seekbar.max
        //allow the screen to turn off again once audio is finished playing
        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    //updating seekbar
    private val mRunnable = Runnable {
        if (mMediaPlayer != null) {
            val mCurrentPosition = mMediaPlayer!!.currentPosition
            binding.seekbar.progress = mCurrentPosition
            val minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition.toLong())
            val seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition.toLong()) - TimeUnit.MINUTES.toSeconds(minutes)
            binding.currentProgress.text = String.format("%02d:%02d", minutes, seconds)
            updateSeekBar()
        }
    }

    private fun updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000)
    }

    override fun onPause() {
        super.onPause()
        mMediaPlayer?.let {
            stopPlaying()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.let {
            stopPlaying()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOG_TAG = "PlaybackFragment"
        private const val ARG_ITEM = "recording_item"
    }
}