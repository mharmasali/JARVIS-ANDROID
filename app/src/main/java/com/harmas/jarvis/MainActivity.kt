package com.harmas.jarvis

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val welcome = TextView(this).apply {
            text = "J.A.R.V.I.S\n\nSystem Initializing..."
            textSize = 26f
            gravity = android.view.Gravity.CENTER
        }

        setContentView(welcome)
    }
}
