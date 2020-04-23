package com.semanientreprise.soundrecorderbox.adapters

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.semanientreprise.soundrecorderbox.presentation.fragments.RecordFragment
import com.semanientreprise.soundrecorderbox.presentation.fragments.SavedRecordingsFragment

class FragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragment_titles = arrayOf("Record", "Saved Recordings")

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return when (position) {
            0 -> RecordFragment.newInstance(position)
            else -> SavedRecordingsFragment.newInstance(position)
        }
    }

    override fun getCount() = fragment_titles.size

    override fun getPageTitle(position: Int) = fragment_titles[position]
}