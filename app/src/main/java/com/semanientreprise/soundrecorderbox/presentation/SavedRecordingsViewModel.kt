package com.semanientreprise.soundrecorderbox.presentation

import android.arch.lifecycle.ViewModel
import com.semanientreprise.soundrecorderbox.models.Recordings
import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData

class SavedRecordingsViewModel : ViewModel() {
    private var recordingsLiveData: ObjectBoxLiveData<Recordings?>? = null
    fun getRecordingsLiveData(recordingsBox: Box<Recordings?>): ObjectBoxLiveData<Recordings?> {
        if (recordingsLiveData == null) {
            recordingsLiveData = ObjectBoxLiveData(recordingsBox.query().build())
        }
        return recordingsLiveData!!
    }
}