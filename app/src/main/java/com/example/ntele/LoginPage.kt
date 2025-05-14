package com.example.ntele

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
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
        setContentView(R.layout.activity_login_page)

        enableEdgeToEdge()

        try {
            val ccp = findViewById<CountryCodePicker>(R.id.countryCodePicker)
            val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
            val sendOtpButton = findViewById<Button>(R.id.send_otp_btn)

            sendOtpButton.setOnClickListener {
                val phoneNumber = phoneEditText.text.toString().trim()

                if (phoneNumber.isEmpty()) {
                    phoneEditText.error = "Please enter phone number"
                    return@setOnClickListener
                }

                val fullPhoneNumber = "+${ccp.selectedCountryCode}$phoneNumber"

                val intent = Intent(this, OTPVerificationPage::class.java)
                intent.putExtra("fullPhoneNumber", fullPhoneNumber)
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
