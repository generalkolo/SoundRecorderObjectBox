package com.semanientreprise.soundrecorderbox;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.NameInDb;

@Entity
public class Recordings {

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
}
