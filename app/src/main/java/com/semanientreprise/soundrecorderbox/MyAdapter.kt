package com.semanientreprise.soundrecorderbox

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class MyAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
    private val fragment_titles = arrayOf("Record", "Saved Recordings")
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return RecordFragment.newInstance(position)
            }
            1 -> {
                return SavedRecordingsFragment.newInstance(position)
            }
        }
        return null
    }

    override fun getCount(): Int {
        return fragment_titles.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return fragment_titles[position]
    }
}