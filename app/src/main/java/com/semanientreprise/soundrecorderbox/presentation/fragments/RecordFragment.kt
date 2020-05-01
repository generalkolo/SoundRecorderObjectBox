package com.semanientreprise.soundrecorderbox.presentation.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Chronometer.OnChronometerTickListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.semanientreprise.soundrecorderbox.R
import com.semanientreprise.soundrecorderbox.databinding.FragmentRecordBinding
import com.semanientreprise.soundrecorderbox.utils.RecordingService
import com.tbruyelle.rxpermissions2.RxPermissions
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
    private lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments!!.getInt(ARG_POSITION)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        rxPermissions = RxPermissions(this)
        setOnClickListeners()
        return binding.root
    }

    private fun setOnClickListeners() {
        binding.Recordbtn.setOnClickListener {
            rxPermissions.request(Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe { granted ->
                        if (granted) {
                            onRecord(mStartRecording)
                            mStartRecording = !mStartRecording
                        }
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