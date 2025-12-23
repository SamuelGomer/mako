package com.rama.mako

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var listView: ListView
    private lateinit var clockManager: ClockManager
    private lateinit var batteryHelper: BatteryManagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        setTheme(R.style.Theme_Mako_Obsidian)
        setContentView(R.layout.view_home)

        timeText = findViewById(R.id.time)
        dateText = findViewById(R.id.date)
        listView = findViewById(R.id.appList)

        // ClockManager
        clockManager = ClockManager(timeText, dateText) { formattedDate ->
            updateDateLine(formattedDate)
        }

        // App list
        val appListHelper = AppListHelper(this, listView)
        appListHelper.setup()

        // ✅ BatteryManagerHelper
        batteryHelper = BatteryManagerHelper(this) { status ->
            updateDateLine(status)
        }
        batteryHelper.register()

        timeText.setOnClickListener {
            val clockIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory("android.intent.category.APP_CLOCK")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val pm = packageManager
            val resolved = clockIntent.resolveActivity(pm)
            if (resolved != null) {
                startActivity(clockIntent)
            } else {
                Toast.makeText(this, "No clock app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryHelper.unregister()
    }

    private fun updateDateLine(text: String) {
        dateText.text = text
    }
}

