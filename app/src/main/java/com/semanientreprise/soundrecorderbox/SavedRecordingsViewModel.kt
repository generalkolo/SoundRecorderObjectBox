package com.semanientreprise.soundrecorderbox

import android.arch.lifecycle.ViewModel
import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData

class SavedRecordingsViewModel : ViewModel() {
    private var recordingsLiveData: ObjectBoxLiveData<Recordings?>? = null
    fun getRecordingsLiveData(recordingsBox: Box<Recordings?>): ObjectBoxLiveData<Recordings?> {
        if (recordingsLiveData == null) {
            recordingsLiveData = ObjectBoxLiveData(recordingsBox.query().build())
        }
        return recordingsLiveData
    }
}