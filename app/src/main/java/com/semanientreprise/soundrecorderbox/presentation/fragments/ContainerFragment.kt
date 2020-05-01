package com.semanientreprise.soundrecorderbox.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.semanientreprise.soundrecorderbox.adapters.FragmentAdapter
import com.semanientreprise.soundrecorderbox.databinding.FragmentContainerBinding

class ContainerFragment : Fragment() {
    private var _binding: FragmentContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentContainerBinding.inflate(inflater, container, false)

        with(binding) {
            pager.adapter = FragmentAdapter(activity?.supportFragmentManager!!)
            tabs.setViewPager(pager)
        }
        return binding.root
    }
}