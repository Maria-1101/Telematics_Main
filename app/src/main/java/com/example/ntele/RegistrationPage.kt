package com.example.ntele

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hbb20.CountryCodePicker
import org.json.JSONObject

class RegistrationPage : AppCompatActivity() {

    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var sendOtpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_page)

        val ccp = findViewById<CountryCodePicker>(R.id.countryCodePicker)
        val loginLink = findViewById<TextView>(R.id.new_user_register)
        phoneNumberEditText = findViewById(R.id.mobile_num_edittext)
        sendOtpButton = findViewById(R.id.send_otp_btn)

        loginLink.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()

            if (!phoneNumber.matches(Regex("^[0-9]{10}$"))) {
                phoneNumberEditText.error = "Enter valid 10-digit number"
                return@setOnClickListener
            }

            val fullPhoneNumber = "+${ccp.selectedCountryCode}$phoneNumber"
            checkIfPhoneNumberExists(fullPhoneNumber)
        }
    }

    private fun checkIfPhoneNumberExists(phoneNumber: String) {
        val databaseRef = FirebaseDatabase.getInstance("https://telematics-a0e1f-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("RegistrationDetails")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isRegistered = false

                for (child in snapshot.children) {
                    val registeredNumber = child.child("phone").getValue(String::class.java)
                    if (registeredNumber == phoneNumber) {
                        isRegistered = true
                        break
                    }
                }

                if (isRegistered) {
                    Toast.makeText(
                        this@RegistrationPage,
                        "Phone number already registered. Please login.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    sendOtpToBackend(phoneNumber)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistrationPage, "Database error: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("Firebase", "Database error: ${error.message}")
            }
        })
    }

    private fun sendOtpToBackend(phoneNumber: String) {
        val url = "https://telematics-zdbu.onrender.com/send-otp" // Replace with your actual backend URL

        val jsonBody = JSONObject().apply {
            put("phoneNumber", phoneNumber)
        }

        val requestBody = jsonBody.toString()

        val request = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                Toast.makeText(this, "OTP Sent!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, OTPVerificationPage::class.java)
                intent.putExtra("phoneNumber", phoneNumber)
                intent.putExtra("source","registration")

                val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
                prefs.edit().putBoolean("isLoggedIn", true).apply()

                startActivity(intent)
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Failed to send OTP: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("OTP", "Error: ${error.message}")
            }
        ) {
            override fun getBody(): ByteArray = requestBody.toByteArray(Charsets.UTF_8)
            override fun getBodyContentType(): String = "application/json"
        }

        Volley.newRequestQueue(this).add(request)
    }
}
