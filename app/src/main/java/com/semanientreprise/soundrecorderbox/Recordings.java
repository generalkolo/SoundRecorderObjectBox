package com.semanientreprise.soundrecorderbox;

import android.os.Parcel;
import android.os.Parcelable;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.NameInDb;

@Entity
public class Recordings implements Parcelable {

    @Id
    public long id;

    @NameInDb("RecordingName")
    private String recording_name;
    @NameInDb("RecordingPath")
    private String recording_path;
    @NameInDb("RecordingLength")
    private long recording_length;
    @NameInDb("RecordingTime")
    private long recording_time_added;

    public Recordings() {}


    public Recordings(long id, String recording_name, String recording_path,
                      long recording_length) {
        this.id = id;
        this.recording_name = recording_name;
        this.recording_path = recording_path;
        this.recording_length = recording_length;
        this.recording_time_added = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRecording_name() {
        return recording_name;
    }

    public void setRecording_name(String recording_name) {
        this.recording_name = recording_name;
    }

    public String getRecording_path() {
        return recording_path;
    }

    public void setRecording_path(String recording_path) {
        this.recording_path = recording_path;
    }

    public long getRecording_length() {
        return recording_length;
    }

    public void setRecording_length(int recording_length) {
        this.recording_length = recording_length;
    }

    public long getRecording_time_added() {
        return recording_time_added;
    }

    public void setRecording_time_added(int recording_time_added) {
        this.recording_time_added = recording_time_added;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.recording_name);
        dest.writeString(this.recording_path);
        dest.writeLong(this.recording_length);
        dest.writeLong(this.recording_time_added);
    }

    protected Recordings(Parcel in) {
        this.id = in.readLong();
        this.recording_name = in.readString();
        this.recording_path = in.readString();
        this.recording_length = in.readLong();
        this.recording_time_added = in.readLong();
    }

    public static final Parcelable.Creator<Recordings> CREATOR = new Parcelable.Creator<Recordings>() {
        @Override
        public Recordings createFromParcel(Parcel source) {
            return new Recordings(source);
        }

        @Override
        public Recordings[] newArray(int size) {
            return new Recordings[size];
        }
    };
}
