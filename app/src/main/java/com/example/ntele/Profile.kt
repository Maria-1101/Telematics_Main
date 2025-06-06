package com.example.ntele

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class Profile : AppCompatActivity() {
    private lateinit var logoutBtn: ImageButton
    private lateinit var cloeButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        logoutBtn = findViewById(R.id.logout_btn)
        cloeButton = findViewById(R.id.close_btn)

        logoutBtn.setOnClickListener {
            val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
            prefs.edit().clear().apply()

            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
