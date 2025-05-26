package com.example.ntele

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit

class RegistrationPage : AppCompatActivity() {

    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var sendOtpButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration_page)

        FirebaseApp.initializeApp(this)
        val ccp = findViewById<CountryCodePicker>(R.id.countryCodePicker)
        val login_link = findViewById<TextView>(R.id.new_user_register)
        firebaseAuth = FirebaseAuth.getInstance()
        phoneNumberEditText = findViewById(R.id.mobile_num_edittext)
        sendOtpButton = findViewById(R.id.send_otp_btn)


        login_link.setOnClickListener{
            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        sendOtpButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()

            // Validation
            if (!phoneNumber.matches(Regex("^[0-9]{10}$"))) {
                phoneNumberEditText.error = "Enter valid 10-digit number"
                return@setOnClickListener
            }

            val fullPhoneNumber = "+${ccp.selectedCountryCode}$phoneNumber"

            // Send OTP
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(fullPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                        // Auto-retrieval or instant verification
                        Toast.makeText(this@RegistrationPage, "Verification Completed Automatically", Toast.LENGTH_SHORT).show()
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(this@RegistrationPage, "Verification Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("PhoneAuth", "onVerificationFailed", e)
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        super.onCodeSent(verificationId, token)
                        Toast.makeText(this@RegistrationPage, "OTP Sent", Toast.LENGTH_SHORT).show()

                        // Move to OTP Verification Page
                        val intent = Intent(this@RegistrationPage, OTPVerificationPage::class.java)
                        intent.putExtra("verificationId", verificationId)
                        intent.putExtra("fullPhoneNumber", fullPhoneNumber)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }
}