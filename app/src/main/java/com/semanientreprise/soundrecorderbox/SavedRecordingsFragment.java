package com.semanientreprise.soundrecorderbox;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.objectbox.Box;

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */

public class SavedRecordingsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.recordings_recView)
    RecyclerView recyclerView;

    Unbinder unbinder;
    @BindView(R.id.search_string)
    EditText searchString;

    private SavedRecordingsAdapter savedRecordingsAdapter;
    private String mParam1;
    private View view;
    private Box<Recordings> myRecordings;
    List<Recordings> recordings;
    private DirectoryFileObserver fileObserver;

    public SavedRecordingsFragment() {}

    public static SavedRecordingsFragment newInstance(int param1) {
        SavedRecordingsFragment fragment = new SavedRecordingsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        fileObserver = new DirectoryFileObserver(android.os.Environment.getExternalStorageDirectory().toString()+ "/Soundbox/");
        fileObserver.startWatching();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_saved_recordings, container, false);
        unbinder = ButterKnife.bind(this, view);

        //initialize the myRecordings Box object and then set it to the Recording.class box by getting a Box object from our
        //MyApplicationClass getBoxStore method.
        myRecordings = ((MyApplicationClass) getActivity().getApplication()).getBoxStore().boxFor(Recordings.class);

        //Setting the Adapter and also the layoutManager of the Adapter
        recordings = myRecordings.getAll();
        savedRecordingsAdapter = new SavedRecordingsAdapter(getActivity(), recordings);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(savedRecordingsAdapter);

        //Setup the ViewModel to listen for new input into the database and then calling the appropriate adapter method
        //to handle it
        SavedRecordingsViewModel model = ViewModelProviders.of(this).get(SavedRecordingsViewModel.class);

        model.getRecordingsLiveData(myRecordings).observe(this, new Observer<List<Recordings>>() {
            @Override
            public void onChanged(@Nullable List<Recordings> recordings) {
                savedRecordingsAdapter.setRecordings(recordings);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_search)
    public void onViewClicked() {
        //get search string from the EditText
        String mSearchString = searchString.getText().toString();

        //Display a Toast if the user didn't enter any search string
        if (mSearchString.isEmpty())
            Toast.makeText(getActivity(), "You cant search for a recording without a name!", Toast.LENGTH_SHORT).show();
        else {
            //Get the recordings that match the users search and set it to our adapters list
            recordings = myRecordings.query().contains(Recordings_.recording_name,mSearchString).build().find();

            //If there exist no recordings with such names, let the user know
            //else set the recording list to the gotten recordings
            if (recordings.size() > 0 ){
                savedRecordingsAdapter.setRecordings(recordings);
            }
            else
                Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.toast_recording_not_exists), mSearchString),Toast.LENGTH_SHORT).show();
        }
    }

    class DirectoryFileObserver extends FileObserver {
        private String asbsolutePath;

        public DirectoryFileObserver(String path) {
            super(path,FileObserver.DELETE);
            asbsolutePath = path;
        }

        @Override
        public void onEvent(int event, @Nullable String path) {
            event &= FileObserver.ALL_EVENTS;
            if((FileObserver.DELETE & event) != 0 ){
                // user deletes a recording file out of the app

                String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                        + "/Soundbox/" + path;

                Log.d("SoundBox/", "File deleted ["
                        + android.os.Environment.getExternalStorageDirectory().toString()
                        + "/Soundbox/" + path + "]");

                // remove file from database and recyclerview
                savedRecordingsAdapter.deleteRecordingWithPath(filePath);
            }
        }
    }
}