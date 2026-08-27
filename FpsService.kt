package com.seu.fpscounter

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.IBinder
import android.view.Choreographer
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class FpsService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var infoTextView: TextView
    private var lastFrameTimeNanos: Long = 0
    private var frameCount = 0
    private var batteryTemp: Float = 0f

    // Recebedor de informações da bateria
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            batteryTemp = temp / 10f // A temperatura vem em décimos de grau
        }
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frameCount++
            if (lastFrameTimeNanos == 0L) {
                lastFrameTimeNanos = frameTimeNanos
            }

            val elapsedNanos = frameTimeNanos - lastFrameTimeNanos
            if (elapsedNanos >= 1_000_000_000) {
                val fps = (frameCount * 1_000_000_000.0 / elapsedNanos).toInt()
                
                // Atualiza o texto com FPS e Temperatura
                infoTextView.text = "FPS: $fps\nTemp: ${batteryTemp}ºC"
                
                frameCount = 0
                lastFrameTimeNanos = frameTimeNanos
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Registra o monitoramento da bateria
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        infoTextView = TextView(this).apply {
            text = "FPS: --\nTemp: --ºC"
            setTextColor(Color.GREEN)
            textSize = 18f
            setBackgroundColor(Color.parseColor("#99000000")) 
            setPadding(20, 20, 20, 20)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 50
        }

        windowManager.addView(infoTextView, params)
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        unregisterReceiver(batteryReceiver)
        if (::infoTextView.isInitialized) {
            windowManager.removeView(infoTextView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
