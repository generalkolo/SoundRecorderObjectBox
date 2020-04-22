package com.semanientreprise.soundrecorderbox.presentation

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.astuetz.PagerSlidingTabStrip
import com.semanientreprise.soundrecorderbox.adapters.FragmentAdapter
import com.semanientreprise.soundrecorderbox.R

class MainActivity : AppCompatActivity() {
    @BindView(R.id.tabs)
    lateinit var tabs: PagerSlidingTabStrip
    @BindView(R.id.pager)
    lateinit var pager: ViewPager
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