package com.semanientreprise.soundrecorderbox.models

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.NameInDb
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull

@Parcelize
@Entity
data class Recordings(
        @Id var id: Long = 0,
        @NameInDb("RecordingName") var recording_name: String? = null,
        @NameInDb("RecordingPath") var recording_path: String? = null,
        @NameInDb("RecordingLength") var recording_length: Long = 0,
        @NameInDb("RecordingTime") var recording_time_added: Long = 0) : Parcelable