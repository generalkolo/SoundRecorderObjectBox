package com.semanientreprise.soundrecorderbox

import android.os.Parcel
import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.NameInDb

@Entity
class Recordings : Parcelable {
    @JvmField
    @Id
    var id: Long = 0
    @NameInDb("RecordingName")
    var recording_name: String? = null
    @NameInDb("RecordingPath")
    var recording_path: String? = null
    @NameInDb("RecordingLength")
    var recording_length: Long = 0
        private set
    @NameInDb("RecordingTime")
    var recording_time_added: Long = 0
        private set

    constructor() {}
    constructor(id: Long, recording_name: String?, recording_path: String?,
                recording_length: Long) {
        this.id = id
        this.recording_name = recording_name
        this.recording_path = recording_path
        this.recording_length = recording_length
        recording_time_added = System.currentTimeMillis()
    }

    fun setRecording_length(recording_length: Int) {
        this.recording_length = recording_length.toLong()
    }

    fun setRecording_time_added(recording_time_added: Int) {
        this.recording_time_added = recording_time_added.toLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(recording_name)
        dest.writeString(recording_path)
        dest.writeLong(recording_length)
        dest.writeLong(recording_time_added)
    }

    protected constructor(`in`: Parcel) {
        id = `in`.readLong()
        recording_name = `in`.readString()
        recording_path = `in`.readString()
        recording_length = `in`.readLong()
        recording_time_added = `in`.readLong()
    }

    companion object {
        val CREATOR: Parcelable.Creator<Recordings> = object : Parcelable.Creator<Recordings?> {
            override fun createFromParcel(source: Parcel): Recordings? {
                return Recordings(source)
            }

            override fun newArray(size: Int): Array<Recordings?> {
                return arrayOfNulls(size)
            }
        }
    }
}