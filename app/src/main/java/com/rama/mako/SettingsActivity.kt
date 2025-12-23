package com.rama.mako

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.view_settings)

        val root = findViewById<View>(android.R.id.content)
        root.setOnApplyWindowInsetsListener { v, insets ->
            val topInset = insets.systemWindowInsetTop
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, v.paddingBottom)
            insets
        }

        val aboutBtn = findViewById<View>(R.id.about_button)
        aboutBtn.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        val closeButton = findViewById<View>(R.id.close_button)
        closeButton.setOnClickListener {
            finish()
        }

        val activateButton = findViewById<View>(R.id.activate_button)

        activateButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Unable to open launcher settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val wallpaperButton = findViewById<View>(R.id.wallpaper_button)
        wallpaperButton.setOnClickListener {
            val wallpaperIntent = Intent(Intent.ACTION_SET_WALLPAPER)
            if (wallpaperIntent.resolveActivity(packageManager) != null) {
                startActivity(wallpaperIntent)
            } else {
                Toast.makeText(this, "No wallpaper app available", Toast.LENGTH_SHORT).show()
            }
        }
    }
}