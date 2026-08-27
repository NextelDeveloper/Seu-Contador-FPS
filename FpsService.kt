package com.seu.fpscounter

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Choreographer
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

class FpsService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var fpsTextView: TextView
    private var lastFrameTimeNanos: Long = 0
    private var frameCount = 0

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frameCount++
            if (lastFrameTimeNanos == 0L) {
                lastFrameTimeNanos = frameTimeNanos
            }

            val elapsedNanos = frameTimeNanos - lastFrameTimeNanos
            if (elapsedNanos >= 1_000_000_000) { // Atualiza a cada 1 segundo
                val fps = (frameCount * 1_000_000_000.0 / elapsedNanos).toInt()
                fpsTextView.text = "FPS: $fps"
                frameCount = 0
                lastFrameTimeNanos = frameTimeNanos
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Configurando o visual do contador
        fpsTextView = TextView(this).apply {
            text = "FPS: --"
            setTextColor(Color.GREEN)
            textSize = 20f
            setBackgroundColor(Color.parseColor("#80000000")) // Fundo semi-transparente
            setPadding(16, 16, 16, 16)
        }

        // Configurando os parâmetros da janela flutuante
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

        windowManager.addView(fpsTextView, params)
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        if (::fpsTextView.isInitialized) {
            windowManager.removeView(fpsTextView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
