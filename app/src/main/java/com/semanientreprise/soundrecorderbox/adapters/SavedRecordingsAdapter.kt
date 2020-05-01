package com.semanientreprise.soundrecorderbox.adapters

import android.content.Context
import android.os.Environment
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.semanientreprise.soundrecorderbox.R
import com.semanientreprise.soundrecorderbox.adapters.SavedRecordingsAdapter.RecordingsViewHolder
import com.semanientreprise.soundrecorderbox.databinding.RecordingsListItemBinding
import com.semanientreprise.soundrecorderbox.models.Recordings
import com.semanientreprise.soundrecorderbox.models.Recordings_
import com.semanientreprise.soundrecorderbox.presentation.fragments.PlayRecordingFragment
import com.semanientreprise.soundrecorderbox.utils.ObjectBox
import io.objectbox.Box
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */
class SavedRecordingsAdapter(private var mContext: Context, private var recordings: List<Recordings>) : RecyclerView.Adapter<RecordingsViewHolder>() {
    private var recording: Recordings? = null
    private val RECORDINGS: Box<Recordings> = ObjectBox.boxStore.boxFor(Recordings::class.java)

    fun setRecordings(recordings: List<Recordings>) {
        this.recordings = recordings
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecordingsViewHolder, position: Int) {
        recording = getItem(position)
        val itemDuration = recording!!.recording_length
        val minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes)

        with(holder) {
            binding.fileNameText.text = recording!!.recording_name
            binding.fileLengthText.text = String.format("%02d:%02d", minutes, seconds)
            binding.fileDateAddedText.text = DateUtils.formatDateTime(
                    mContext,
                    recording!!.recording_time_added,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NUMERIC_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_YEAR
            )

            // define an on click listener to open PlaybackFragment
            binding.cardView.setOnClickListener {
                try {
                    val playbackFragment = PlayRecordingFragment().newInstance(getItem(holder.adapterPosition))
                    val transaction = (mContext as androidx.fragment.app.FragmentActivity)
                            .supportFragmentManager
                            .beginTransaction()
                    playbackFragment.show(transaction, "dialog_playback")
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "exception", e)
                }
            }

            binding.cardView.setOnLongClickListener {
                val dialogOptions = ArrayList<String>()
                dialogOptions.addAll(listOf(
                        mContext.getString(R.string.dialog_file_rename),
                        mContext.getString(R.string.dialog_file_delete)
                ))
                val items = dialogOptions.toTypedArray<CharSequence>()
                // Option Dialog Created
                AlertDialog.Builder(mContext).apply {
                    setTitle(mContext.getString(R.string.dialog_title_options))
                    setItems(items) { _, item ->
                        when (item) {
                            0 -> renameFileDialog(holder.adapterPosition)
                            1 -> deleteFileDialog(holder.adapterPosition)
                        }
                    }
                    setCancelable(true)
                    setNegativeButton(mContext.getString(R.string.dialog_action_cancel)
                    ) { dialog, _ -> dialog.dismiss() }
                }.create().show()
                false
            }
        }
    }

    private fun renameFileDialog(position: Int) { // File rename dialog
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.dialog_rename_recording, null)
        val input = view.findViewById<EditText>(R.id.new_name)

        AlertDialog.Builder(mContext).apply {
            setTitle(mContext.getString(R.string.dialog_title_rename))
            setCancelable(true)
            setPositiveButton(mContext.getString(R.string.dialog_action_ok)
            ) { dialog, _ ->
                try {
                    val value = input.text.toString().trim { it <= ' ' } + ".mp4"
                    rename(position, value)
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "exception", e)
                }
                dialog.dismiss()
            }
            setNegativeButton(mContext.getString(R.string.dialog_action_cancel)
            ) { dialog, _ -> dialog.dismiss() }
            setView(view)
        }.create().show()
    }

    private fun rename(position: Int, name: String) { //rename a file
        val mFilePath = "${Environment.getExternalStorageDirectory().absolutePath}/Soundbox/$name"

        val f = File(mFilePath)
        if (f.exists() && !f.isDirectory) { //file name is not unique, cannot rename file.
            Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show()
        } else { //file name is unique, rename file
            val oldFilePath = File(getItem(position).recording_path!!)
            oldFilePath.renameTo(f)
            //Get a Box for the Recordings.class
            val recordings = ObjectBox.boxStore.boxFor(Recordings::class.java)
            //Get the particular box item that we intend to rename
            val mRecordingToChange = recordings[recording!!.id]
            mRecordingToChange.recording_name = name
            mRecordingToChange.recording_path = f.absolutePath
            //Put the modified object back into our database
            recordings.put(mRecordingToChange)
            notifyItemChanged(position)
        }
    }

    private fun deleteFileDialog(position: Int) {
        // File delete confirm
        AlertDialog.Builder(mContext).apply {
            setTitle(mContext.getString(R.string.dialog_title_delete))
            setMessage(mContext.getString(R.string.dialog_text_delete))
            setCancelable(true)
            setPositiveButton(mContext.getString(R.string.dialog_action_yes)
            ) { dialog, _ ->
                try {
                    //remove item from ObjectBox, RecyclerView, and storage (user's phone)
                    remove(position)
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "exception", e)
                }
                dialog.dismiss()
            }
            setNegativeButton(mContext.getString(R.string.dialog_action_no)
            ) { dialog, _ -> dialog.dismiss() }
        }.create().show()
    }

    //remove item from database, RecyclerView and storage
    private fun remove(position: Int) {
        //delete file from storage
        val file = File(getItem(position).recording_path!!)
        file.delete()
        //Get the particular box item that we intend to delete
        val recordingToDelete = RECORDINGS[recording!!.id]
        //Delete the object from the ObjectBox
        RECORDINGS.remove(recordingToDelete)
        notifyItemRemoved(position)
        Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_delete), getItem(position).recording_name), Toast.LENGTH_SHORT).show()
    }

    fun deleteRecordingWithPath(filePath: String) { //user deletes a saved recording out of the application through another application
        val record = RECORDINGS.query().equal(Recordings_.recording_path, filePath).build().findFirst()
        if (record != null) {
            RECORDINGS.remove(record.id)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingsViewHolder {
        val binding = RecordingsListItemBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return RecordingsViewHolder(binding)
    }

    class RecordingsViewHolder(val binding: RecordingsListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = recordings.size

    private fun getItem(position: Int): Recordings = recordings[position]

    companion object {
        private const val LOG_TAG = "FileViewerAdapter"
    }
}