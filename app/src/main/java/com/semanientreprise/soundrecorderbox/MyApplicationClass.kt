package com.semanientreprise.soundrecorderbox

import android.app.Application
import io.objectbox.BoxStore

class MyApplicationClass : Application() {
    var boxStore: BoxStore? = null
    override fun onCreate() {
        super.onCreate()
        boxStore = MyObjectBox.builder().androidContext(this@MyApplicationClass).build()
    }

}