package com.semanientreprise.soundrecorderbox;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.objectbox.Box;

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */
public class SavedRecordingsAdapter extends RecyclerView.Adapter<SavedRecordingsAdapter.RecordingsViewHolder>{

    private static final String LOG_TAG = "FileViewerAdapter";

    private List<Recordings> recordings;
    private Recordings recording;
    private Context mContext;
    private Box<Recordings> RECORDINGS;

    public void setRecordings(List<Recordings> recordings){
        this.recordings = recordings;
        notifyDataSetChanged();
    }

    public SavedRecordingsAdapter(Context context, List<Recordings> recordings) {
        super();
        mContext = context;
        this.recordings = recordings;

        //Get a Box for the Recordings.class POJO
        RECORDINGS = ((MyApplicationClass)mContext.getApplicationContext()).getBoxStore().boxFor(Recordings.class);
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

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ArrayList<String> option_entries = new ArrayList<>();
                option_entries.add(mContext.getString(R.string.dialog_file_rename));
                option_entries.add(mContext.getString(R.string.dialog_file_delete));

                final CharSequence[] items = option_entries.toArray(new CharSequence[option_entries.size()]);

                // Option Dialog Created
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.dialog_title_options));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            renameFileDialog(holder.getAdapterPosition());
                        }
                        if (item == 1) {
                            deleteFileDialog(holder.getAdapterPosition());
                        }
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();

                return false;
            }
        });
    }

    public void renameFileDialog (final int position) {
        // File rename dialog
        AlertDialog.Builder renameFileBuilder = new AlertDialog.Builder(mContext);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_rename_recording, null);

        final EditText input = view.findViewById(R.id.new_name);

        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename));
        renameFileBuilder.setCancelable(true);
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            String value = input.getText().toString().trim() + ".mp4";
                            rename(position, value);
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.dismiss();
                    }
                });
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        renameFileBuilder.setView(view);
        AlertDialog alert = renameFileBuilder.create();
        alert.show();
    }

    public void rename(int position, String name) {
        //rename a file
        String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/Soundbox/" + name;
        File f = new File(mFilePath);

        if (f.exists() && !f.isDirectory()) {
            //file name is not unique, cannot rename file.
            Toast.makeText(mContext,
                    String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show();
        } else {
            //file name is unique, rename file
            File oldFilePath = new File(getItem(position).getRecording_path());
            oldFilePath.renameTo(f);

            //Get a Box for the Recordings.class
            Box<Recordings> RECORDINGS = ((MyApplicationClass)mContext.getApplicationContext()).getBoxStore().boxFor(Recordings.class);

            //Get the particular box item that we intend to rename
            Recordings mRecordingToChange = RECORDINGS.get(recording.getId());
            mRecordingToChange.setRecording_name(name);
            mRecordingToChange.setRecording_path(f.getAbsolutePath());

            //Put the modified object back into our database
            RECORDINGS.put(mRecordingToChange);
            notifyItemChanged(position);
        }
    }

    public void deleteFileDialog (final int position) {
        // File delete confirm
        AlertDialog.Builder confirmDelete = new AlertDialog.Builder(mContext);
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete));
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete));
        confirmDelete.setCancelable(true);
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            //remove item from ObjectBox, RecyclerView, and storage (user's phone)
                            remove(position);

                        } catch (Exception e) {
                            Log.e(LOG_TAG, "exception", e);
                        }

                        dialog.dismiss();
                    }
                });
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = confirmDelete.create();
        alert.show();
    }

    private void remove(int position) {
        //remove item from database, RecyclerView and storage

        //delete file from storage
        File file = new File(getItem(position).getRecording_path());
        file.delete();

        //Get the particular box item that we intend to delete
        Recordings recordingToDelete = RECORDINGS.get(recording.getId());

        //Delete the object from the ObjectBox
        RECORDINGS.remove(recordingToDelete);
        notifyItemRemoved(position);

        Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_delete), getItem(position).getRecording_name()),Toast.LENGTH_SHORT).show();
    }

    public void deleteRecordingWithPath(String filePath) {
        //user deletes a saved recording out of the application through another application
        Recordings record = RECORDINGS.query().equal(Recordings_.recording_path,filePath).build().findFirst();

        if (record != null){
            RECORDINGS.remove(record.getId());
            notifyDataSetChanged();
        }
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
