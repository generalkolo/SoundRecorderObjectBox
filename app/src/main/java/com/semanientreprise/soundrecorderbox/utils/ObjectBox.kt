package com.semanientreprise.soundrecorderbox.utils

import android.content.Context
import com.semanientreprise.soundrecorderbox.models.MyObjectBox
import io.objectbox.BoxStore

object ObjectBox {
    lateinit var boxStore: BoxStore
        private set

    fun init(context: Context) {
        boxStore = MyObjectBox.builder().androidContext(context.applicationContext)
                .build()
    }
}