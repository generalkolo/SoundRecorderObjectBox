package com.semanientreprise.soundrecorderbox.presentation.fragments

import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import com.semanientreprise.soundrecorderbox.R
import com.semanientreprise.soundrecorderbox.adapters.SavedRecordingsAdapter
import com.semanientreprise.soundrecorderbox.databinding.FragmentSavedRecordingsBinding
import com.semanientreprise.soundrecorderbox.models.Recordings
import com.semanientreprise.soundrecorderbox.models.Recordings_
import com.semanientreprise.soundrecorderbox.presentation.SavedRecordingsViewModel
import com.semanientreprise.soundrecorderbox.utils.ObjectBox
import io.objectbox.Box

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */
class SavedRecordingsFragment : Fragment() {
    private var _binding: FragmentSavedRecordingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var savedRecordingsAdapter: SavedRecordingsAdapter
    private var myRecordings: Box<Recordings> = ObjectBox.boxStore.boxFor(Recordings::class.java)
    var recordings: List<Recordings> = emptyList()
    private var fileObserver: DirectoryFileObserver? = null

    private var mParam1: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
        }
        fileObserver = DirectoryFileObserver("${Environment.getExternalStorageDirectory()}/Soundbox}")
        fileObserver?.startWatching()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        _binding = FragmentSavedRecordingsBinding.inflate(inflater, container, false)
        init()
        initObservers()
        initClickListeners()
        return binding.root
    }

    private fun init() {
        //initialize the myRecordings Box object and then set it to the Recording.class box by getting a Box object from our
        //MyApplicationClass getBoxStore method.
        myRecordings = ObjectBox.boxStore.boxFor(Recordings::class.java)
        //Setting the Adapter and also the layoutManager of the Adapter
        recordings = myRecordings.all
        savedRecordingsAdapter = SavedRecordingsAdapter(this@SavedRecordingsFragment.requireContext(), recordings)
        initRecyclerViewWithAdapter(savedRecordingsAdapter)
    }

    private fun initRecyclerViewWithAdapter(savedRecordingsAdapter: SavedRecordingsAdapter) {
        with(binding.recordingsRecView) {
            itemAnimator = DefaultItemAnimator()
            adapter = savedRecordingsAdapter
            setHasFixedSize(true)
        }
    }

    private fun initObservers() {
        //Setup the ViewModel to listen for new input into the database and then calling the appropriate adapter method
        //to handle it
        val model = ViewModelProvider(this).get(SavedRecordingsViewModel::class.java)
        model.getRecordingsLiveData(myRecordings).observe(viewLifecycleOwner,
                Observer { recordings -> savedRecordingsAdapter.setRecordings(recordings) })
    }

    private fun initClickListeners() {
        binding.btnSearch.setOnClickListener {
            //get search string from the EditText
            val mSearchString = binding.searchString.text.toString()
            //Display a Toast if the user didn't enter any search string
            if (mSearchString.isEmpty()) {
                Toast.makeText(activity, "You cant search for a recording without a name!", Toast.LENGTH_SHORT).show()
            } else {
                //Get the recordings that match the users search and set it to our adapters list
                recordings = myRecordings.query().contains(Recordings_.recording_name, mSearchString).build().find()
                //If there exist no recordings with such names, let the user know
                //else set the recording list to the gotten recordings
                if (recordings.isNotEmpty()) {
                    savedRecordingsAdapter.setRecordings(recordings)
                } else {
                    Toast.makeText(activity, String.format(activity!!.getString(R.string.toast_recording_not_exists), mSearchString), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    internal inner class DirectoryFileObserver(absolutePath: String) : FileObserver(absolutePath, DELETE) {
        override fun onEvent(event: Int, path: String?) {
            var event = event
            event = event and ALL_EVENTS
            if (DELETE and event != 0) { // user deletes a recording file out of the app
                val filePath = "${Environment.getExternalStorageDirectory()}/Soundbox/$path)"
                Log.d("SoundBox", "File deleted [${Environment.getExternalStorageDirectory()}/Soundbox/$path]")
                // remove file from database and recyclerview
                savedRecordingsAdapter.deleteRecordingWithPath(filePath)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PARAM1 = "param1"

        @JvmStatic
        fun newInstance(param1: Int): SavedRecordingsFragment {
            val fragment = SavedRecordingsFragment()
            val args = Bundle()
            args.putInt(ARG_PARAM1, param1)
            fragment.arguments = args
            return fragment
        }
    }
}