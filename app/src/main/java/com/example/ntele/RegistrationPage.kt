package com.example.ntele

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hbb20.CountryCodePicker

class RegistrationPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration_page)

        val ccp = findViewById<CountryCodePicker>(R.id.countryCodePicker)
        val phoneEditText = findViewById<EditText>(R.id.editTextPhone)
        val login_link = findViewById<TextView>(R.id.new_user_register)

        login_link.setOnClickListener{
            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        val send_otp_obile_number = findViewById<Button>(R.id.send_otp_btn)
        send_otp_obile_number.setOnClickListener{
            val phoneNumber = phoneEditText.text.toString().trim()

            if (phoneNumber.isEmpty()) {
                phoneEditText.error = "Please enter phone number"
                return@setOnClickListener
            }

            val fullPhoneNumber = "+${ccp.selectedCountryCode}$phoneNumber"

            val intent = Intent(this, OTPVerificationPage::class.java)
            intent.putExtra("fullPhoneNumber", fullPhoneNumber)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}