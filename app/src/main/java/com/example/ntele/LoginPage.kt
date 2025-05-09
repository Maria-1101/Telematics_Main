package com.example.ntele

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hbb20.CountryCodePicker

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page) // First, call setContentView

        enableEdgeToEdge()



        // Now access the CountryCodePicker after the view is set
        val ccp = findViewById<CountryCodePicker>(R.id.countryCodePicker)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)

        // Retrieve the full phone number with country code
        val fullPhoneNumber = "+${ccp.selectedCountryCode}${phoneEditText.text}"

        // Apply window insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
