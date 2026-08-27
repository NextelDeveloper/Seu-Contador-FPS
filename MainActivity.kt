package com.seu.fpscounter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val btnStart = Button(this).apply { text = "Iniciar FPS" }
        val btnStop = Button(this).apply { text = "Parar FPS" }

        layout.addView(btnStart)
        layout.addView(btnStop)
        setContentView(layout)

        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                // Pede permissão se não tiver
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(this, "Permita a sobreposição e tente novamente", Toast.LENGTH_SHORT).show()
            } else {
                startService(Intent(this, FpsService::class.java))
            }
        }

        btnStop.setOnClickListener {
            stopService(Intent(this, FpsService::class.java))
        }
    }
}
