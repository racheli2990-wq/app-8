package com.studiopro.daw

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import java.io.File

class MainActivity : Activity() {
    private val bg = Color.rgb(13, 16, 29)
    private val panel = Color.rgb(25, 30, 49)
    private val accent = Color.rgb(130, 105, 255)
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordingFile: File? = null
    private var eq: Equalizer? = null
    private var bass: BassBoost? = null
    private var reverb: PresetReverb? = null
    private lateinit var status: TextView
    private lateinit var recordButton: Button
    private lateinit var playButton: Button
    private var recording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 71)
        }
        buildStudio()
    }

    private fun rounded(color: Int, radius: Float = 22f): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = radius }

    private fun label(text: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView =
        TextView(this).apply { this.text = text; textSize = size; setTextColor(color); if (bold) typeface = Typeface.DEFAULT_BOLD }

    private fun button(text: String, color: Int = accent): Button =
        Button(this).apply { this.text = text; setTextColor(Color.WHITE); textSize = 13f; isAllCaps = false; background = rounded(color, 18f); stateListAnimator = null }

    private fun buildStudio() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(20, 18, 20, 14) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; orientation = LinearLayout.HORIZONTAL }
        header.addView(LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(label("STUDIO PRO", 12f, 0xFF9B8BFF.toInt(), true)); addView(label("הסטודיו שלך. בלי גבולות.", 22f, Color.WHITE, true)) }, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(label("●  OFFLINE", 11f, 0xFF65E6B3.toInt(), true))
        content.addView(header)
        content.addView(space(18))
        val hero = card()
        hero.addView(label("🎚  WORKSPACE", 12f, 0xFFB5A9FF.toInt(), true))
        hero.addView(label("הפקה מוזיקלית בכף היד", 20f, Color.WHITE, true))
        hero.addView(label("הקלטה • מיקס • אפקטים • יצירה", 13f, 0xFFADB5CE.toInt()))
        val wave = label("▂▅▃▇▅▂▆▃▇▅▂▅▇▃▆▂▅▇▃▅▂▆▇▃▅", 22f, 0xFF8C7BFF.toInt(), true)
        wave.gravity = Gravity.CENTER
        hero.addView(wave)
        content.addView(hero)
        content.addView(space(14))

        val transport = card()
        transport.addView(label("TRANSPORT", 12f, 0xFFB5A9FF.toInt(), true))
        status = label("מוכן להקלטה · 00:00", 15f, Color.WHITE, true)
        transport.addView(status)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; }
        recordButton = button("●  הקלט", 0xFFE34F76.toInt())
        playButton = button("▶  נגן", 0xFF5545A8.toInt())
        val stop = button("■  עצור", 0xFF414B65.toInt())
        controls.addView(recordButton, LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(3, 10, 3, 0) })
        controls.addView(playButton, LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(3, 10, 3, 0) })
        controls.addView(stop, LinearLayout.LayoutParams(0, 52, 1f).apply { setMargins(3, 10, 3, 0) })
        transport.addView(controls)
        content.addView(transport)
        content.addView(space(14))

        val tracks = card()
        tracks.addView(label("TRACKS  /  ערוצים", 12f, 0xFFB5A9FF.toInt(), true))
        tracks.addView(label("🎤  ערוץ הקלטה ראשי", 16f, Color.WHITE, true))
        tracks.addView(label("הקלטה נשמרת כקובץ M4A במכשיר", 12f, 0xFFADB5CE.toInt()))
        val meter = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100; progress = 72; progressTintList = android.content.res.ColorStateList.valueOf(0xFF8C7BFF.toInt()); progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0xFF343B55.toInt()) }
        tracks.addView(meter, LinearLayout.LayoutParams(-1, 10).apply { topMargin = 14 })
        content.addView(tracks)
        content.addView(space(14))

        val fx = card()
        fx.addView(label("✨  VOCAL FX & MIX", 12f, 0xFFB5A9FF.toInt(), true))
        fx.addView(label("עצב את הסאונד שלך", 19f, Color.WHITE, true))
        addSlider(fx, "EQ · איזון תדרים", "בהיר", 0, 100, 50) { v -> eq?.let { e -> try { if (e.numberOfBands > 0) e.setBandLevel(0, ((v - 50) * 60).toShort()) } catch (_: Exception) {} } }
        addSlider(fx, "Bass Boost · באס", "עוצמה", 0, 100, 25) { v -> bass?.let { try { it.setStrength((v * 10).toShort()) } catch (_: Exception) {} } }
        addSlider(fx, "Reverb · חלל והדהוד", "אולם", 0, 100, 35) { v -> reverb?.let { try { it.preset = if (v > 65) PresetReverb.PRESET_LARGEHALL else if (v > 25) PresetReverb.PRESET_MEDIUMROOM else PresetReverb.PRESET_NONE } catch (_: Exception) {} } }
        addSlider(fx, "Vocal presence · נוכחות", "עדין", 0, 100, 50) { }
        content.addView(fx)
        content.addView(space(14))

        val tools = card()
        tools.addView(label("MORE TOOLS", 12f, 0xFFB5A9FF.toInt(), true))
        tools.addView(label("🎛  מיקסר     🎹  קלידים     🎧  מוניטור", 14f))
        tools.addView(label("💾  שמירת הקלטות מקומית · ללא חשבון", 13f, 0xFFADB5CE.toInt()))
        val about = button("אודות Studio Pro", 0xFF343B55.toInt())
        about.setOnClickListener { android.app.AlertDialog.Builder(this).setTitle("Studio Pro").setMessage("אולפן מוזיקה נייד ליצירה, הקלטה ועיצוב סאונד.\nגרסה 1.0 · עובד גם ללא חיבור לאינטרנט.").setPositiveButton("סגור", null).show() }
        tools.addView(about)
        content.addView(tools)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(label("STUDIO PRO  •  CREATE YOUR SOUND", 10f, 0xFF69738F.toInt()).apply { gravity = Gravity.CENTER; setPadding(0, 12, 0, 2) })
        setContentView(root)

        recordButton.setOnClickListener { if (recording) stopRecording() else startRecording() }
        playButton.setOnClickListener { playRecording() }
        stop.setOnClickListener { stopRecording(); stopPlayback() }
    }

    private fun space(h: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, h) }
    private fun card() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(17, 16, 17, 16); background = rounded(panel); elevation = 2f }
    private fun addSlider(parent: LinearLayout, title: String, hint: String, min: Int, max: Int, initial: Int, change: (Int) -> Unit) {
        parent.addView(label(title, 14f, Color.WHITE, true))
        val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val seek = SeekBar(this).apply { this.max = max - min; progress = initial - min; progressTintList = android.content.res.ColorStateList.valueOf(0xFF9B8BFF.toInt()); thumbTintList = android.content.res.ColorStateList.valueOf(Color.WHITE); setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) { change(p + min) }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        }) }
        line.addView(seek, LinearLayout.LayoutParams(0, 42, 1f))
        line.addView(label(hint, 10f, 0xFFADB5CE.toInt()))
        parent.addView(line)
    }

    private fun startRecording() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 71); status.text = "נדרשת הרשאת מיקרופון"; return
        }
        try {
            val dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: filesDir
            recordingFile = File(dir, "StudioPro_" + System.currentTimeMillis() + ".m4a")
            recorder = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(this) else @Suppress("DEPRECATION") MediaRecorder()
            recorder?.apply { setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); setAudioEncoder(MediaRecorder.AudioEncoder.AAC); setAudioEncodingBitRate(192000); setAudioSamplingRate(44100); setOutputFile(recordingFile!!.absolutePath); prepare(); start() }
            recording = true; recordButton.text = "■  סיים הקלטה"; status.text = "● מקליט עכשיו…"
        } catch (e: Exception) { status.text = "שגיאת הקלטה: " + (e.message ?: "לא ידוע"); recording = false }
    }

    private fun stopRecording() {
        if (!recording) return
        try { recorder?.stop() } catch (_: Exception) {}
        recorder?.release(); recorder = null; recording = false
        recordButton.text = "●  הקלט"
        status.text = "ההקלטה נשמרה · " + (recordingFile?.name ?: "")
    }

    private fun playRecording() {
        val file = recordingFile
        if (file == null || !file.exists()) { status.text = "עדיין אין הקלטה לניגון"; return }
        stopPlayback()
        try {
            player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnPreparedListener { mp ->
                    val session = mp.audioSessionId
                    try {
                        eq = Equalizer(0, session).apply { enabled = true }
                        bass = BassBoost(0, session).apply { enabled = true }
                        reverb = PresetReverb(0, session).apply { enabled = true; preset = PresetReverb.PRESET_MEDIUMROOM }
                    } catch (_: Exception) {}
                    mp.start(); status.text = "▶ מנגן הקלטה"
                }
                setOnCompletionListener { status.text = "הניגון הסתיים" }
                prepareAsync()
            }
        } catch (e: Exception) { status.text = "לא ניתן לנגן: " + (e.message ?: "") }
    }

    private fun stopPlayback() {
        try { player?.stop() } catch (_: Exception) {}
        player?.release(); player = null
        try { eq?.release(); bass?.release(); reverb?.release() } catch (_: Exception) {}
        eq = null; bass = null; reverb = null
    }

    override fun onDestroy() { stopRecording(); stopPlayback(); super.onDestroy() }
}
