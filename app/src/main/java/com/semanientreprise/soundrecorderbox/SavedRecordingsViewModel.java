package com.semanientreprise.soundrecorderbox;

import android.arch.lifecycle.ViewModel;

import io.objectbox.Box;
import io.objectbox.android.ObjectBoxLiveData;

public class SavedRecordingsViewModel extends ViewModel {

    private ObjectBoxLiveData<Recordings> recordingsLiveData;

    public ObjectBoxLiveData<Recordings> getRecordingsLiveData(Box<Recordings> recordingsBox) {
        if (recordingsLiveData == null) {
            recordingsLiveData = new ObjectBoxLiveData<>(recordingsBox.query().build());
        }
        return recordingsLiveData;
    }
}
