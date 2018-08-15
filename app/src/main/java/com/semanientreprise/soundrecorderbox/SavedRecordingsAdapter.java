package com.semanientreprise.soundrecorderbox;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */
public class SavedRecordingsAdapter extends RecyclerView.Adapter<SavedRecordingsAdapter.RecordingsViewHolder>{

    private static final String LOG_TAG = "FileViewerAdapter";

    private List<Recordings> recordings;
    private Recordings recording;
    private Context mContext;

    public void setRecordings(List<Recordings> recordings){
        this.recordings = recordings;
        notifyDataSetChanged();
    }

    public SavedRecordingsAdapter(Context context, List<Recordings> recordings) {
        super();
        mContext = context;
        this.recordings = recordings;
    }

    @Override
    public void onBindViewHolder(final RecordingsViewHolder holder, int position) {

        recording = getItem(position);
        long itemDuration = recording.getRecording_length();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);

        holder.vName.setText(recording.getRecording_name());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vDateAdded.setText(
            DateUtils.formatDateTime(
                mContext,
                recording.getRecording_time_added(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
            )
        );

        // define an on click listener to open PlaybackFragment
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlayRecordingFragment playbackFragment = new PlayRecordingFragment().newInstance(getItem(holder.getAdapterPosition()));

                    FragmentTransaction transaction = ((FragmentActivity) mContext)
                            .getSupportFragmentManager()
                            .beginTransaction();

                    playbackFragment.show(transaction, "dialog_playback");

                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }
            }
        });
    }

    @Override
    public RecordingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recordings_list_item, parent, false);
        mContext = parent.getContext();
        return new RecordingsViewHolder(itemView);
    }

    public static class RecordingsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.file_name_text) TextView vName;
        @BindView(R.id.file_length_text) TextView vLength;
        @BindView(R.id.file_date_added_text) TextView vDateAdded;
        @BindView(R.id.card_view) View cardView;

        private RecordingsViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    private Recordings getItem(int position) {
        return recordings.get(position);
    }
}
