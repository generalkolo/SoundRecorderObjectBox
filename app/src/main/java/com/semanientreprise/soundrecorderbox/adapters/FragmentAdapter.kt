package com.semanientreprise.soundrecorderbox.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.semanientreprise.soundrecorderbox.presentation.fragments.RecordFragment
import com.semanientreprise.soundrecorderbox.presentation.fragments.SavedRecordingsFragment

class FragmentAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    private val fragment_titles = arrayOf("Record", "Saved Recordings")

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                RecordFragment.newInstance(position)
            }
            else -> {
                return  SavedRecordingsFragment.newInstance(position)
            }
        }
    }

    override fun getCount() = fragment_titles.size

    override fun getPageTitle(position: Int) = fragment_titles[position]
}