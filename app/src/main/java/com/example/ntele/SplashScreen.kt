package com.example.ntele

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val ring1: View = findViewById(R.id.ring1)
        val ring2: View = findViewById(R.id.ring2)
        val ring3: View = findViewById(R.id.ring3)
        val logo: ImageView = findViewById(R.id.logo)
        val appName: TextView = findViewById(R.id.app_name)

        val ringAnim = AnimationUtils.loadAnimation(this, R.anim.ring_expand)
        val zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }


        ring1.startAnimation(ringAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            ring2.startAnimation(ringAnim)
        }, 300)

        Handler(Looper.getMainLooper()).postDelayed({
            ring3.startAnimation(ringAnim)
        }, 600)

        Handler(Looper.getMainLooper()).postDelayed({
            ring1.visibility = View.GONE
            ring2.visibility = View.GONE
            ring3.visibility = View.GONE
            logo.visibility = View.VISIBLE
            appName.visibility = View.VISIBLE
            logo.startAnimation(zoomIn)
            appName.startAnimation(zoomIn)

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@SplashActivity, LoginOrRegistration::class.java)
                startActivity(intent)
                finish() // closes SplashActivity so it can't be returned to
            }, 1000) // delay slightly to let zoom-in play

        }, 1600)
    }

}