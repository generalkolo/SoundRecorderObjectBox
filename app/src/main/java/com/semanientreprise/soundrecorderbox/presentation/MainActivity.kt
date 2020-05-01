package com.semanientreprise.soundrecorderbox.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.semanientreprise.soundrecorderbox.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setSupportActionBar(binding.toolbar.toolbar)

        val view = binding.root
        setContentView(view)
    }
}