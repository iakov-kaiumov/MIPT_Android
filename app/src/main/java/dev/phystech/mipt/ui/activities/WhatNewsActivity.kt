package dev.phystech.mipt.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import dev.phystech.mipt.R
import dev.phystech.mipt.repositories.Storage

class WhatNewsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_what_news)

        Storage.shared.setFirstRun(false)

        val rlAccept: RelativeLayout = findViewById(R.id.rlAccept)

        rlAccept.setOnClickListener {
            val intent: Intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}