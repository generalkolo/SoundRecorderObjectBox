package com.semanientreprise.soundrecorderbox;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.objectbox.Box;

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */

public class SavedRecordingsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    Unbinder unbinder;

    private SavedRecordingsAdapter savedRecordingsAdapter;
    private String mParam1;
    private View view;
    private Box<Recordings> myRecordings;
    List<Recordings> recordings;

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
}
