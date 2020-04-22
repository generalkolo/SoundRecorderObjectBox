package com.semanientreprise.soundrecorderbox

import android.app.Application
import com.semanientreprise.soundrecorderbox.utils.ObjectBox

class MyApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}