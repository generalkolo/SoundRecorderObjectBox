package com.semanientreprise.soundrecorderbox

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.ColorFilter
import android.graphics.LightingColorFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */
class PlayRecordingFragment : DialogFragment() {
    @JvmField
    @BindView(R.id.file_name_text_view)
    var mFileNameTextView: TextView? = null
    @JvmField
    @BindView(R.id.seekbar)
    var mSeekBar: SeekBar? = null
    @JvmField
    @BindView(R.id.current_progress_text_view)
    var mCurrentProgressTextView: TextView? = null
    @JvmField
    @BindView(R.id.fab_play)
    var mPlayButton: FloatingActionButton? = null
    @JvmField
    @BindView(R.id.file_length_text_view)
    var mFileLengthTextView: TextView? = null
    var unbinder: Unbinder? = null
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
        recording = arguments.getParcelable(ARG_ITEM)
        val itemDuration = recording.recording_length
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration)
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.fragment_play_recording, null)
        unbinder = ButterKnife.bind(this, view)
        val filter: ColorFilter = LightingColorFilter(resources.getColor(R.color.colorPrimary), resources.getColor(R.color.colorPrimary))
        mSeekBar!!.progressDrawable.colorFilter = filter
        mSeekBar!!.thumb.colorFilter = filter
        mSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer!!.seekTo(progress)
                    mHandler.removeCallbacks(mRunnable)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer!!.currentPosition.toLong())
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer!!.currentPosition.toLong()) - TimeUnit.MINUTES.toSeconds(minutes)
                    mCurrentProgressTextView!!.text = String.format("%02d:%02d", minutes, seconds)
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
                    mCurrentProgressTextView!!.text = String.format("%02d:%02d", minutes, seconds)
                    updateSeekBar()
                }
            }
        })
        mFileNameTextView!!.text = recording!!.recording_name
        mFileLengthTextView!!.text = String.format("%02d:%02d", minutes, seconds)
        builder.setView(view)
        // request a window without the title
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        return builder.create()
    }

    @OnClick(R.id.fab_play)
    fun onViewClicked() {
        onPlay(isMediaPlaying)
        isMediaPlaying = !isMediaPlaying
    }

    override fun onStart() {
        super.onStart()
        //set transparent background
        val window = dialog.window
        window.setBackgroundDrawableResource(android.R.color.transparent)
        //disable buttons from dialog
        val alertDialog = dialog as AlertDialog
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = false
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).isEnabled = false
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).isEnabled = false
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer != null) {
            stopPlaying()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMediaPlayer != null) {
            stopPlaying()
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
        mPlayButton!!.setImageResource(R.drawable.ic_pause)
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(recording!!.recording_path)
            mMediaPlayer!!.prepare()
            mSeekBar!!.max = mMediaPlayer!!.duration
            mMediaPlayer!!.setOnPreparedListener { mMediaPlayer!!.start() }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
        mMediaPlayer!!.setOnCompletionListener { stopPlaying() }
        updateSeekBar()
        //keep screen on while playing audio
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun prepareMediaPlayerFromPoint(progress: Int) { //set mediaPlayer to start from middle of the audio file
        mMediaPlayer = MediaPlayer()
        try {
            mMediaPlayer!!.setDataSource(recording!!.recording_path)
            mMediaPlayer!!.prepare()
            mSeekBar!!.max = mMediaPlayer!!.duration
            mMediaPlayer!!.seekTo(progress)
            mMediaPlayer!!.setOnCompletionListener { stopPlaying() }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
        //keep screen on while playing audio
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun pausePlaying() {
        mPlayButton!!.setImageResource(R.drawable.ic_media_play)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer!!.pause()
    }

    private fun resumePlaying() {
        mPlayButton!!.setImageResource(R.drawable.ic_pause)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer!!.start()
        updateSeekBar()
    }

    private fun stopPlaying() {
        mPlayButton!!.setImageResource(R.drawable.ic_media_play)
        mHandler.removeCallbacks(mRunnable)
        mMediaPlayer!!.stop()
        mMediaPlayer!!.reset()
        mMediaPlayer!!.release()
        mMediaPlayer = null
        mSeekBar!!.progress = mSeekBar!!.max
        isMediaPlaying = !isMediaPlaying
        mCurrentProgressTextView!!.text = mFileLengthTextView!!.text
        mSeekBar!!.progress = mSeekBar!!.max
        //allow the screen to turn off again once audio is finished playing
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    //updating mSeekBar
    private val mRunnable = Runnable {
        if (mMediaPlayer != null) {
            val mCurrentPosition = mMediaPlayer!!.currentPosition
            mSeekBar!!.progress = mCurrentPosition
            val minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition.toLong())
            val seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition.toLong()) - TimeUnit.MINUTES.toSeconds(minutes)
            mCurrentProgressTextView!!.text = String.format("%02d:%02d", minutes, seconds)
            updateSeekBar()
        }
    }

    private fun updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // TODO: inflate a fragment view
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }

    companion object {
        private const val LOG_TAG = "PlaybackFragment"
        private const val ARG_ITEM = "recording_item"
    }
}