package com.semanientreprise.soundrecorderbox;

import android.app.Application;

import io.objectbox.BoxStore;

public class MyApplicationClass extends Application {

    public BoxStore boxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        boxStore = MyObjectBox.builder().androidContext(MyApplicationClass.this).build();
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}