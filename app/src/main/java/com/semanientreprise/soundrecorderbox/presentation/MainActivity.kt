package com.semanientreprise.soundrecorderbox.presentation

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.astuetz.PagerSlidingTabStrip
import com.semanientreprise.soundrecorderbox.adapters.FragmentAdapter
import com.semanientreprise.soundrecorderbox.R

class MainActivity : AppCompatActivity() {
    @BindView(R.id.tabs)
    lateinit var tabs: PagerSlidingTabStrip
    @BindView(R.id.pager)
    lateinit var pager: androidx.viewpager.widget.ViewPager
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        pager.adapter = FragmentAdapter(supportFragmentManager)
        tabs.setViewPager(pager)
        setSupportActionBar(toolbar)
    }
}