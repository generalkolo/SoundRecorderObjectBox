package com.semanientreprise.soundrecorderbox

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import com.astuetz.PagerSlidingTabStrip

class MainActivity : AppCompatActivity() {
    @JvmField
    @BindView(R.id.tabs)
    var tabs: PagerSlidingTabStrip? = null
    @JvmField
    @BindView(R.id.pager)
    var pager: ViewPager? = null
    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        pager!!.adapter = MyAdapter(supportFragmentManager)
        tabs!!.setViewPager(pager)
        setSupportActionBar(toolbar)
    }
}