package com.semanientreprise.soundrecorderbox.presentation.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.semanientreprise.soundrecorderbox.R
import com.semanientreprise.soundrecorderbox.databinding.FragmentRecordBinding
import com.semanientreprise.soundrecorderbox.utils.RecordingService
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [RecordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private var position = 0
    private var mRecordPromptCount = 0
    private var mStartRecording = true
    private val RECORD_AUDIO_REQUEST_CODE = 10010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments!!.getInt(ARG_POSITION)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.Recordbtn.setOnClickListener {
            //TODO: Change with RxPermissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionToRecordAudio
            }
            onRecord(mStartRecording)
            mStartRecording = !mStartRecording
        }
    }

    @get:RequiresApi(api = Build.VERSION_CODES.M)
    val permissionToRecordAudio: Unit
        get() {
            if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        RECORD_AUDIO_REQUEST_CODE)
            }
        }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) { // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.size == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(activity, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Recording Start/Stop
    private fun onRecord(start: Boolean) {
        val intent = Intent(activity, RecordingService::class.java)
        if (start) {
            // start recording
            binding.Recordbtn.setImageResource(R.drawable.ic_stop)
            Toast.makeText(activity, R.string.toast_recording_start, Toast.LENGTH_SHORT).show()
            val folder = File("${Environment.getExternalStorageDirectory()}/Soundbox")
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir()
            }
            //start Chronometer
            with(binding) {
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
                chronometer.onChronometerTickListener = OnChronometerTickListener {
                    when (mRecordPromptCount) {
                        0 -> {
                            recordingStatusText.text = getString(R.string.recording_in_progress_holder, ".")
                        }
                        1 -> {
                            recordingStatusText.text = getString(R.string.recording_in_progress_holder, "..")
                        }
                        2 -> {
                            recordingStatusText.text = getString(R.string.recording_in_progress_holder, "...")
                            mRecordPromptCount = -1
                        }
                    }
                    mRecordPromptCount++
                }
            }
            activity?.let {
                //start RecordingService
                it.startService(intent)
                //keep screen on while recording
                it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            binding.recordingStatusText.text = getString(R.string.recording_in_progress_holder, ".")
            mRecordPromptCount++
        } else {
            //stop recording
            with(binding) {
                Recordbtn.setImageResource(R.drawable.ic_mic)
                chronometer.stop()
                chronometer.base = SystemClock.elapsedRealtime()
                recordingStatusText.text = getString(R.string.record_prompt)
            }
            activity?.let {
                it.stopService(intent)
                //allow the screen to turn off again once recording is finished
                it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_POSITION = "position"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment Record_Fragment.
         */
        @JvmStatic
        fun newInstance(position: Int): RecordFragment {
            val f = RecordFragment()
            val b = Bundle()
            b.putInt(ARG_POSITION, position)
            f.arguments = b
            return f
        }
    }
}