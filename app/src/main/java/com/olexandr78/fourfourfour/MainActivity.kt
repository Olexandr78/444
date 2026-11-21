package com.olexandr78.fourfourfour

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private enum class Screen { INCOMING, CONNECTED, DECLINED, ENDED }
    private var screen = Screen.INCOMING
    private var attempts = 0
    private var line = 0
    private var muted = false
    private var speaker = false
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private lateinit var root: LinearLayout

    private val phrases = listOf(
        "Ты всё-таки ответил.", "Почему ты сбросил?", "Я знаю, что ты здесь.",
        "Посмотри на экран.", "Не оборачивайся.", "444 — это не номер.",
        "Ты слышишь звонок?", "Я уже звонил тебе."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.statusBarColor = Color.rgb(5, 5, 5)
        window.navigationBarColor = Color.rgb(5, 5, 5)
        hideSystemUi()
        vibrator = if (Build.VERSION.SDK_INT >= 31) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        render()
        startEffects()
    }

    private fun hideSystemUi() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

    private fun startEffects() {
        stopEffects()
        if (screen != Screen.INCOMING) return
        try {
            // Use the actual ringtone selected by the device owner in Android settings.
            val ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(
                this, RingtoneManager.TYPE_RINGTONE
            ) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri)?.also { sound ->
                sound.audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                if (Build.VERSION.SDK_INT >= 28) sound.isLooping = true
                sound.play()
            }
        } catch (_: Exception) { }
        try {
            val pattern = longArrayOf(0, 650, 450, 650, 1100)
            if (Build.VERSION.SDK_INT >= 26) vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) { }
    }

    private fun stopEffects() {
        try { ringtone?.stop() } catch (_: Exception) { }
        ringtone = null
        try { vibrator?.cancel() } catch (_: Exception) { }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun bg(color: Int, radius: Int = 20) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            this.text = text; textSize = size; setTextColor(color); gravity = Gravity.CENTER
            if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setPadding(dp(8), dp(5), dp(8), dp(5))
        }

    private fun button(text: String, color: Int, action: () -> Unit) = Button(this).apply {
        this.text = text; setTextColor(Color.WHITE); textSize = 15f; isAllCaps = false
        background = bg(color, 50); setPadding(dp(18), dp(12), dp(18), dp(12))
        setOnClickListener { action() }
    }

    private fun gap(height: Int) { root.addView(View(this), LinearLayout.LayoutParams(1, dp(height))) }

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(22), dp(24), dp(18)); setBackgroundColor(Color.rgb(5, 5, 5))
        }
        setContentView(root)
        root.addView(label("UNKNOWN CALLER", 12f, Color.rgb(120, 120, 120)))
        gap(34)
        root.addView(label("444", 54f, Color.rgb(238, 238, 238), true))
        val status = when (screen) {
            Screen.INCOMING -> "INCOMING CALL"
            Screen.CONNECTED -> "CONNECTED"
            Screen.DECLINED -> "CALL DECLINED"
            Screen.ENDED -> "DISCONNECTED"
        }
        root.addView(label(status, 13f, Color.rgb(180, 45, 45), true))
        gap(38)
        root.addView(label(if (screen == Screen.INCOMING) "☎" else if (screen == Screen.CONNECTED) "◉" else "✕",
            58f, Color.rgb(145, 20, 25), true))
        gap(22)

        when (screen) {
            Screen.INCOMING -> {
                root.addView(label("unknown number  •  unknown origin", 13f, Color.GRAY))
                gap(58)
                val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
                actions.addView(button("Отклонить", Color.rgb(170, 25, 32)) {
                    attempts++; screen = Screen.DECLINED; stopEffects(); render()
                }, LinearLayout.LayoutParams(0, dp(58), 1f).apply { marginEnd = dp(10) })
                actions.addView(button("Ответить", Color.rgb(20, 125, 62)) {
                    screen = Screen.CONNECTED; line = (line + 2) % phrases.size; stopEffects(); render()
                }, LinearLayout.LayoutParams(0, dp(58), 1f).apply { marginStart = dp(10) })
                root.addView(actions)
            }
            Screen.CONNECTED -> {
                val message = label("«" + phrases[line] + "»", 19f, Color.WHITE, true)
                message.background = bg(Color.rgb(19, 19, 19), 18)
                root.addView(message, LinearLayout.LayoutParams(-1, dp(112)))
                gap(20)
                root.addView(label(if (muted) "MICROPHONE: OFF" else "MICROPHONE: ON", 12f, Color.GRAY))
                root.addView(label(if (speaker) "SPEAKER: ON" else "SPEAKER: OFF", 12f, Color.GRAY))
                gap(22)
                root.addView(button("Следующая фраза", Color.rgb(55, 55, 55)) {
                    line = (line + 1) % phrases.size; render()
                }, LinearLayout.LayoutParams(-1, dp(54)))
                gap(10)
                root.addView(button("Микрофон: вкл/выкл", Color.rgb(40, 40, 40)) {
                    muted = !muted; render()
                }, LinearLayout.LayoutParams(-1, dp(54)))
                gap(10)
                root.addView(button("Динамик: вкл/выкл", Color.rgb(40, 40, 40)) {
                    speaker = !speaker; render()
                }, LinearLayout.LayoutParams(-1, dp(54)))
                gap(18)
                root.addView(button("Завершить вызов", Color.rgb(170, 25, 32)) {
                    screen = Screen.ENDED; stopEffects(); render()
                }, LinearLayout.LayoutParams(-1, dp(56)))
            }
            Screen.DECLINED, Screen.ENDED -> {
                root.addView(label(
                    if (screen == Screen.DECLINED) "ТЫ УВЕРЕН, ЧТО ЭТО БЫЛ ПОСЛЕДНИЙ ЗВОНОК?"
                    else "ВЫЗОВ ЗАВЕРШЁН. НО НОМЕР ОСТАЛСЯ.",
                    16f, Color.rgb(180, 45, 45), true))
                gap(35)
                root.addView(button("Повторить сценарий", Color.rgb(135, 20, 25)) {
                    screen = Screen.INCOMING; muted = false; speaker = false; render(); startEffects()
                }, LinearLayout.LayoutParams(-1, dp(58)))
            }
        }
        val spacer = View(this)
        root.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        root.addView(label("SYSTEM LOG  •  " + stamp, 10f, Color.rgb(90, 90, 90)))
        root.addView(label("CONNECTION_ID: 444     ATTEMPTS: " + attempts, 10f, Color.rgb(75, 75, 75)))
        gap(12)
        root.addView(label("HORROR SIMULATOR  •  NO REAL CALLS", 10f, Color.rgb(105, 105, 105)))
    }

    override fun onDestroy() { stopEffects(); super.onDestroy() }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (screen == Screen.CONNECTED) { screen = Screen.ENDED; stopEffects(); render() }
        else super.onBackPressed()
    }
}
