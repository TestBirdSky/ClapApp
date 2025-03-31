package com.example.clapapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.clapapp.activities.SplashFirstActivity

/**
 * Date：2025/3/31
 * Describe:
 * com.example.clapapp.ClapAppActivity
 */
class ClapAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, SplashFirstActivity::class.java))
        finish()
    }

}