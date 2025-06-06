package com.example.ntele

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.qamar.curvedbottomnaviagtion.findFont
import java.util.*

class UserDetails : AppCompatActivity() {
    private lateinit var dobInput: TextInputEditText
    private lateinit var mobileNumText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var mobileNumVerifiedIcon: ImageView
    private lateinit var emailVerifiedIcon: ImageView
    private lateinit var nameEdittext: TextInputEditText
    private lateinit var stateEdittext: TextInputEditText
    private lateinit var cityEdittext: TextInputEditText
    private lateinit var postalCodeEdittext: TextInputEditText
    private lateinit var genderSpinner: MaterialAutoCompleteTextView
    private lateinit var getStarted: Button
    private lateinit var getStartedEnabled: Button
    private lateinit var countryCodePicker: com.hbb20.CountryCodePicker


    private var verifiedPhoneNumber: String? = null
    private var emailVerified: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        val database = FirebaseDatabase.getInstance()
        val databaseRef = database.getReference("RegistrationDetails")

        dobInput = findViewById(R.id.dob_input)
        val emailVerifyText = findViewById<TextView>(R.id.verify_text)
        mobileNumText = findViewById(R.id.editTextPhone)
        emailEditText =  findViewById(R.id.email_input)
        mobileNumVerifiedIcon =  findViewById(R.id.verified_MobileNumber_icon)
        nameEdittext =  findViewById(R.id.name_input)
        stateEdittext = findViewById(R.id.state_input)
        cityEdittext =  findViewById(R.id.city_input)
        postalCodeEdittext = findViewById(R.id.postalCode_input)
        genderSpinner = findViewById(R.id.gender_input)
        emailVerifiedIcon = findViewById(R.id.verified_emailID_icon)
        getStarted = findViewById(R.id.get_started)
        getStartedEnabled = findViewById(R.id.get_started_clickable)
        countryCodePicker = findViewById(R.id.countryCodePicker)

        nameEdittext.setText(intent.getStringExtra("name"))
        dobInput.setText(intent.getStringExtra("dob"))
        stateEdittext.setText(intent.getStringExtra("state"))
        cityEdittext.setText(intent.getStringExtra("city"))
        postalCodeEdittext.setText(intent.getStringExtra("postalCode"))
        genderSpinner.setText(intent.getStringExtra("gender"), false)

        val verifiedEmail = intent.getStringExtra("verifiedEmail")
        emailVerified = intent.getBooleanExtra("isEmailVerified", false)
        if (!verifiedEmail.isNullOrEmpty() && emailVerified) {
            emailEditText.setText(verifiedEmail)
            if (emailVerified) {
                emailEditText.isEnabled = false
                emailEditText.isFocusable = false
                emailVerifiedIcon.visibility = View.VISIBLE
                emailVerifyText.visibility = View.GONE
            } else {
                emailVerifiedIcon.visibility = View.GONE
            }
        }

        val mobileNum = intent.getStringExtra("phoneNumber")
        verifiedPhoneNumber = intent.getStringExtra("verifiedPhoneNumber")
        if (!verifiedPhoneNumber.isNullOrEmpty() || !mobileNum.isNullOrEmpty()) {
            val fullNumber = verifiedPhoneNumber ?: mobileNum ?: ""
            // Remove country code (assumes "+" prefix and country code length up to 4)
            val countryCode = countryCodePicker.selectedCountryCodeWithPlus
            val localNumber = if (fullNumber.startsWith(countryCode)) {
                fullNumber.removePrefix(countryCode)
            } else {
                fullNumber
            }

            mobileNumText.setText(localNumber)
            mobileNumText.isEnabled = false
            mobileNumText.isFocusable = false
            mobileNumVerifiedIcon.visibility = View.VISIBLE
        } else {
            mobileNumVerifiedIcon.visibility = View.GONE
        }

        // ✅ Email Verify Click with Validation
        // Email verify action
        emailVerifyText.setOnClickListener {
            val enteredEmail = emailEditText.text.toString().trim()
            if (enteredEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, EmailOTPVerification::class.java).apply {
                putExtra("email", enteredEmail)
                putExtra("name", nameEdittext.text.toString())
                putExtra("dob", dobInput.text.toString())
                putExtra("state", stateEdittext.text.toString())
                putExtra("city", cityEdittext.text.toString())
                putExtra("postalCode", postalCodeEdittext.text.toString())
                putExtra("gender", genderSpinner.text.toString())
                putExtra("verifiedPhoneNumber", verifiedPhoneNumber)
                putExtra("source","registration")
            }
            startActivity(intent)
        }

        getStartedEnabled.setOnClickListener {
            val name = nameEdittext.text.toString().trim()
            val dob = dobInput.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val countryCode = countryCodePicker.selectedCountryCodeWithPlus
            val phoneNumber = mobileNumText.text.toString().trim()
            val fullPhoneNumber = if (phoneNumber.startsWith("+")) phoneNumber else countryCode + phoneNumber
            val state = stateEdittext.text.toString().trim()
            val city = cityEdittext.text.toString().trim()
            val postalCode = postalCodeEdittext.text.toString().trim()
            val gender = genderSpinner.text.toString().trim()
            val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)

            prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userName", name)
                .apply()

            val userData = mapOf(
                "name" to name,
                "dob" to dob,
                "email" to email,
                "phone" to fullPhoneNumber,
                "state" to state,
                "city" to city,
                "postalCode" to postalCode,
                "gender" to gender
            )

            databaseRef.child(name).setValue(userData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User data saved", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, AboutVehicle::class.java)
                    intent.putExtra("name", name)
                    Log.d(" name User","" + name)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
            }
        }


        dobInput.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val ddmmyyyy = "DDMMYYYY"
            private val cal = Calendar.getInstance()

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    var clean = s.toString().replace("[^\\d]".toRegex(), "")

                    var sel = clean.length
                    var i = 2
                    while (i <= clean.length && i < 8) {
                        sel++
                        i += 2
                    }

                    if (clean.length < 8) {
                        clean += ddmmyyyy.substring(clean.length)
                    } else {
                        val day = clean.substring(0, 2).toInt()
                        var month = clean.substring(2, 4).toInt()
                        var year = clean.substring(4, 8).toInt()

                        // Validate month
                        month = when {
                            month < 1 -> 1
                            month > 12 -> 12
                            else -> month
                        }

                        // Set calendar month and year to get correct max day
                        cal.set(Calendar.MONTH, month - 1)
                        cal.set(Calendar.YEAR, year)
                        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val adjustedDay = if (day > maxDay) maxDay else day

                        clean = String.format("%02d%02d%04d", adjustedDay, month, year)
                    }

                    // Format to dd/mm/yyyy
                    val formatted = "${clean.substring(0, 2)}/${clean.substring(2, 4)}/${clean.substring(4, 8)}"

                    current = formatted
                    dobInput.removeTextChangedListener(this)
                    dobInput.setText(formatted)
                    dobInput.setSelection(if (sel < formatted.length) sel else formatted.length)
                    dobInput.addTextChangedListener(this)
                }
            }
        })

        val genderInput: MaterialAutoCompleteTextView = findViewById(R.id.gender_input)
        val genderOptions = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        genderInput.setAdapter(adapter)

        addFormListeners()
        checkFormCompletion()
    }

    // ✅ Check if form is completely filled
    private fun checkFormCompletion() {
        val isFormFilled = nameEdittext.text.toString().isNotEmpty()
                && dobInput.text.toString().isNotEmpty()
                && emailEditText.text.toString().isNotEmpty()
                && emailVerified
                && mobileNumText.text.toString().isNotEmpty()
                && !verifiedPhoneNumber.isNullOrEmpty()
                && stateEdittext.text.toString().isNotEmpty()
                && cityEdittext.text.toString().isNotEmpty()
                && postalCodeEdittext.text.toString().isNotEmpty()
                && genderSpinner.text.toString().isNotEmpty()

        if (isFormFilled) {
            getStarted.visibility = View.GONE
            getStartedEnabled.visibility = View.VISIBLE
        } else {
            getStarted.visibility = View.VISIBLE
            getStartedEnabled.visibility = View.GONE
        }
    }

    // ✅ Add listeners to form inputs
    private fun addFormListeners() {
        val fields = listOf(
            nameEdittext, dobInput, emailEditText,
            stateEdittext, cityEdittext, postalCodeEdittext
        )

        for (field in fields) {
            field.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) = checkFormCompletion()
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        genderSpinner.setOnItemClickListener { _, _, _, _ ->
            checkFormCompletion()
        }
    }

    // ✅ Email validation function
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
