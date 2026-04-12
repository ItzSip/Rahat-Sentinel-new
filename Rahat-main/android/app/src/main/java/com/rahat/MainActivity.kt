package com.rahat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Allow content to draw behind system bars for full gradient effect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RahatApp()
        }
    }
}