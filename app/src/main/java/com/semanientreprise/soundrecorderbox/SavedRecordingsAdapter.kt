package com.semanientreprise.soundrecorderbox

import android.content.Context
import android.os.Environment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.semanientreprise.soundrecorderbox.SavedRecordingsAdapter.RecordingsViewHolder
import io.objectbox.Box
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Edet Ebenezer on 08/15/2018.
 */
class SavedRecordingsAdapter(private var mContext: Context, private var recordings: List<Recordings>) : RecyclerView.Adapter<RecordingsViewHolder>() {
    private var recording: Recordings? = null
    private val RECORDINGS: Box<Recordings>
    fun setRecordings(recordings: List<Recordings>) {
        this.recordings = recordings
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecordingsViewHolder, position: Int) {
        recording = getItem(position)
        val itemDuration = recording!!.recording_length
        val minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes)
        holder.vName!!.text = recording!!.recording_name
        holder.vLength!!.text = String.format("%02d:%02d", minutes, seconds)
        holder.vDateAdded!!.text = DateUtils.formatDateTime(
                mContext,
                recording!!.recording_time_added,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NUMERIC_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_YEAR
        )
        // define an on click listener to open PlaybackFragment
        holder.cardView!!.setOnClickListener {
            try {
                val playbackFragment = PlayRecordingFragment().newInstance(getItem(holder.adapterPosition))
                val transaction = (mContext as FragmentActivity)
                        .supportFragmentManager
                        .beginTransaction()
                playbackFragment.show(transaction, "dialog_playback")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "exception", e)
            }
        }
        holder.cardView!!.setOnLongClickListener {
            val option_entries = ArrayList<String>()
            option_entries.add(mContext.getString(R.string.dialog_file_rename))
            option_entries.add(mContext.getString(R.string.dialog_file_delete))
            val items = option_entries.toTypedArray<CharSequence>()
            // Option Dialog Created
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle(mContext.getString(R.string.dialog_title_options))
            builder.setItems(items) { dialog, item ->
                if (item == 0) {
                    renameFileDialog(holder.adapterPosition)
                }
                if (item == 1) {
                    deleteFileDialog(holder.adapterPosition)
                }
            }
            builder.setCancelable(true)
            builder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel)
            ) { dialog, id -> dialog.dismiss() }
            val alert = builder.create()
            alert.show()
            false
        }
    }

    fun renameFileDialog(position: Int) { // File rename dialog
        val renameFileBuilder = AlertDialog.Builder(mContext)
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.dialog_rename_recording, null)
        val input = view.findViewById<EditText>(R.id.new_name)
        renameFileBuilder.setTitle(mContext.getString(R.string.dialog_title_rename))
        renameFileBuilder.setCancelable(true)
        renameFileBuilder.setPositiveButton(mContext.getString(R.string.dialog_action_ok)
        ) { dialog, id ->
            try {
                val value = input.text.toString().trim { it <= ' ' } + ".mp4"
                rename(position, value)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "exception", e)
            }
            dialog.dismiss()
        }
        renameFileBuilder.setNegativeButton(mContext.getString(R.string.dialog_action_cancel)
        ) { dialog, id -> dialog.dismiss() }
        renameFileBuilder.setView(view)
        val alert = renameFileBuilder.create()
        alert.show()
    }

    fun rename(position: Int, name: String) { //rename a file
        var mFilePath = Environment.getExternalStorageDirectory().absolutePath
        mFilePath += "/Soundbox/$name"
        val f = File(mFilePath)
        if (f.exists() && !f.isDirectory) { //file name is not unique, cannot rename file.
            Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_exists), name),
                    Toast.LENGTH_SHORT).show()
        } else { //file name is unique, rename file
            val oldFilePath = File(getItem(position).recording_path)
            oldFilePath.renameTo(f)
            //Get a Box for the Recordings.class
            val RECORDINGS = (mContext.applicationContext as MyApplicationClass).boxStore.boxFor(Recordings::class.java)
            //Get the particular box item that we intend to rename
            val mRecordingToChange = RECORDINGS[recording!!.id]
            mRecordingToChange.recording_name = name
            mRecordingToChange.recording_path = f.absolutePath
            //Put the modified object back into our database
            RECORDINGS.put(mRecordingToChange)
            notifyItemChanged(position)
        }
    }

    fun deleteFileDialog(position: Int) { // File delete confirm
        val confirmDelete = AlertDialog.Builder(mContext)
        confirmDelete.setTitle(mContext.getString(R.string.dialog_title_delete))
        confirmDelete.setMessage(mContext.getString(R.string.dialog_text_delete))
        confirmDelete.setCancelable(true)
        confirmDelete.setPositiveButton(mContext.getString(R.string.dialog_action_yes)
        ) { dialog, id ->
            try { //remove item from ObjectBox, RecyclerView, and storage (user's phone)
                remove(position)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "exception", e)
            }
            dialog.dismiss()
        }
        confirmDelete.setNegativeButton(mContext.getString(R.string.dialog_action_no)
        ) { dialog, id -> dialog.dismiss() }
        val alert = confirmDelete.create()
        alert.show()
    }

    private fun remove(position: Int) { //remove item from database, RecyclerView and storage
//delete file from storage
        val file = File(getItem(position).recording_path)
        file.delete()
        //Get the particular box item that we intend to delete
        val recordingToDelete = RECORDINGS[recording!!.id]
        //Delete the object from the ObjectBox
        RECORDINGS.remove(recordingToDelete)
        notifyItemRemoved(position)
        Toast.makeText(mContext, String.format(mContext.getString(R.string.toast_file_delete), getItem(position).recording_name), Toast.LENGTH_SHORT).show()
    }

    fun deleteRecordingWithPath(filePath: String?) { //user deletes a saved recording out of the application through another application
        val record = RECORDINGS.query().equal(Recordings_.recording_path, filePath).build().findFirst()
        if (record != null) {
            RECORDINGS.remove(record.id)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recordings_list_item, parent, false)
        mContext = parent.context
        return RecordingsViewHolder(itemView)
    }

    class RecordingsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        @JvmField
        @BindView(R.id.file_name_text)
        var vName: TextView? = null
        @JvmField
        @BindView(R.id.file_length_text)
        var vLength: TextView? = null
        @JvmField
        @BindView(R.id.file_date_added_text)
        var vDateAdded: TextView? = null
        @JvmField
        @BindView(R.id.card_view)
        var cardView: View? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    override fun getItemCount(): Int {
        return recordings.size
    }

    private fun getItem(position: Int): Recordings {
        return recordings[position]
    }

    companion object {
        private const val LOG_TAG = "FileViewerAdapter"
    }

    init {
        //Get a Box for the Recordings.class POJO
        RECORDINGS = (mContext.applicationContext as MyApplicationClass).boxStore.boxFor(Recordings::class.java)
    }
}