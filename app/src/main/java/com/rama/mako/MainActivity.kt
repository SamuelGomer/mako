package com.rama.mako

import android.app.Activity
import android.content.*
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var timeText: TextView
    private lateinit var dateText: TextView
    private lateinit var listView: ListView

    private val handler = Handler(Looper.getMainLooper())
    private var batteryPercent: String? = null

    /* ---------- Lifecycle ---------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen (API 26–29 safe)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        setContentView(R.layout.view_home)

        timeText = findViewById(R.id.time)
        dateText = findViewById(R.id.date)
        listView = findViewById(R.id.appList)

        setupAppList()
        startClock()
        registerBatteryReceiver()

        val timeView = findViewById<View>(R.id.time)
        timeText.setOnClickListener {
            val clockIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory("android.intent.category.APP_CLOCK") // literal string
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // Check if an app exists to handle this intent
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
        unregisterReceiver(batteryReceiver)
        handler.removeCallbacksAndMessages(null)
    }

    /* ---------- Time & Date ---------- */

    private fun startClock() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val runnable = object : Runnable {
            override fun run() {
                val now = Date()
                timeText.text = timeFormat.format(now)
                updateDateLine(dateFormat.format(now))
                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }

    private fun updateDateLine(date: String) {
        dateText.text = buildStatusLine(date, batteryPercent)
    }

    private fun buildStatusLine(date: String?, battery: String?): String {
        return listOfNotNull(date, battery).joinToString("  |  ")
    }

    /* ---------- Battery ---------- */

    private fun registerBatteryReceiver() {
        registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            val temp =
                (((intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)) / 10f) * 9f / 5f) + 32f

            if (level >= 0 && scale > 0) {
                batteryPercent =
                    "BAT: ${(level * 100 / scale.toFloat()).toInt()}% :: ${temp.toInt()}°F"
                updateDateLine(
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date())
                )
            }
        }
    }

    /* ---------- App list ---------- */

    private fun setupAppList() {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString().lowercase(Locale.getDefault()) }

        val labels = apps.map { it.loadLabel(pm).toString() }

        listView.adapter = ArrayAdapter(
            this,
            R.layout.app_list_item,
            R.id.text1,
            labels
        )

        listView.setOnItemClickListener { _, _, position, _ ->
            if (position >= apps.size) return@setOnItemClickListener

            val app = apps[position]
            val launchIntent = Intent().apply {
                setClassName(app.activityInfo.packageName, app.activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Safely try to start the app
            try {
                startActivity(launchIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "App not found or uninstalled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val settingsButton = findViewById<View>(R.id.settings_button)

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
