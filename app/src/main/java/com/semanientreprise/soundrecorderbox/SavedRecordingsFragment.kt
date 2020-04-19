package com.semanientreprise.soundrecorderbox

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Environment
import android.os.FileObserver
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import io.objectbox.Box

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */
class SavedRecordingsFragment : Fragment() {
    @JvmField
    @BindView(R.id.recordings_recView)
    var recyclerView: RecyclerView? = null
    var unbinder: Unbinder? = null
    @JvmField
    @BindView(R.id.search_string)
    var searchString: EditText? = null
    private var savedRecordingsAdapter: SavedRecordingsAdapter? = null
    private var mParam1: String? = null
    private var view: View? = null
    private var myRecordings: Box<Recordings?>? = null
    var recordings: List<Recordings?>? = null
    private var fileObserver: DirectoryFileObserver? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
        }
        fileObserver = DirectoryFileObserver(Environment.getExternalStorageDirectory().toString() + "/Soundbox/")
        fileObserver!!.startWatching()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_saved_recordings, container, false)
        unbinder = ButterKnife.bind(this, view)
        //initialize the myRecordings Box object and then set it to the Recording.class box by getting a Box object from our
//MyApplicationClass getBoxStore method.
        myRecordings = (activity.application as MyApplicationClass).boxStore.boxFor(Recordings::class.java)
        //Setting the Adapter and also the layoutManager of the Adapter
        recordings = myRecordings.getAll()
        savedRecordingsAdapter = SavedRecordingsAdapter(activity, recordings)
        recyclerView!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        //newest to oldest order (database stores from oldest to newest)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView!!.layoutManager = linearLayoutManager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = savedRecordingsAdapter
        //Setup the ViewModel to listen for new input into the database and then calling the appropriate adapter method
//to handle it
        val model = ViewModelProviders.of(this).get(SavedRecordingsViewModel::class.java)
        model.getRecordingsLiveData(myRecordings).observe(this, Observer { recordings -> savedRecordingsAdapter!!.setRecordings(recordings) })
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder!!.unbind()
    }

    @OnClick(R.id.btn_search)
    fun onViewClicked() { //get search string from the EditText
        val mSearchString = searchString!!.text.toString()
        //Display a Toast if the user didn't enter any search string
        if (mSearchString.isEmpty()) Toast.makeText(activity, "You cant search for a recording without a name!", Toast.LENGTH_SHORT).show() else { //Get the recordings that match the users search and set it to our adapters list
            recordings = myRecordings!!.query().contains(Recordings_.recording_name, mSearchString).build().find()
            //If there exist no recordings with such names, let the user know
//else set the recording list to the gotten recordings
            if (recordings!!.size > 0) {
                savedRecordingsAdapter!!.setRecordings(recordings)
            } else Toast.makeText(activity, String.format(activity.getString(R.string.toast_recording_not_exists), mSearchString), Toast.LENGTH_SHORT).show()
        }
    }

    internal inner class DirectoryFileObserver(private val asbsolutePath: String) : FileObserver(asbsolutePath, DELETE) {
        override fun onEvent(event: Int, path: String?) {
            var event = event
            event = event and ALL_EVENTS
            if (DELETE and event != 0) { // user deletes a recording file out of the app
                val filePath = (Environment.getExternalStorageDirectory().toString()
                        + "/Soundbox/" + path)
                Log.d("SoundBox/", "File deleted ["
                        + Environment.getExternalStorageDirectory().toString()
                        + "/Soundbox/" + path + "]")
                // remove file from database and recyclerview
                savedRecordingsAdapter!!.deleteRecordingWithPath(filePath)
            }
        }

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