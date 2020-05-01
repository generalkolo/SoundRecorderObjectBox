package com.semanientreprise.soundrecorderbox

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.semanientreprise.soundrecorderbox.utils.ObjectBox

class MyApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}