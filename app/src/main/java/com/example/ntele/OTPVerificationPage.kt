package com.example.ntele

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OTPVerificationPage : AppCompatActivity() {
    private lateinit var verifyButton: Button
    private lateinit var verifyButtonClickable: Button
    private lateinit var newOTPTime: TextView
    private lateinit var mobileNumToVerify: TextView
    private lateinit var otpBoxes: List<EditText>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var verificationId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otpverification_page)

        FirebaseApp.initializeApp(this)

        verifyButton = findViewById(R.id.verify_button)
        firebaseAuth =  FirebaseAuth.getInstance()
        verificationId = intent.getStringExtra("verificationId")?:return
        otpBoxes = listOf(
            findViewById(R.id.otp_1),
            findViewById(R.id.otp_2),
            findViewById(R.id.otp_3),
            findViewById(R.id.otp_4),
            findViewById(R.id.otp_5),
            findViewById(R.id.otp_6)
        )

        verifyButton.setOnClickListener {
            val otp = otpBoxes.joinToString("") { it.text.toString().trim() }
            if (otp.length == 6) {
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupOtpWatcher() {
        otpBoxes.forEach {
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val otp = otpBoxes.joinToString("") { box -> box.text.toString().trim() }
                    verifyButtonClickable.visibility = if (otp.length == 6) View.VISIBLE else View.GONE
                    verifyButton.visibility = if(otp.length == 6) View.GONE else View.VISIBLE
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show()
                    // TODO: Redirect to the next screen
                } else {
                    Toast.makeText(this, "Verification failed!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
