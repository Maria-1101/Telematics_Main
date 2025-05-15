package com.example.ntele

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class UserDetails : AppCompatActivity() {
    private lateinit var dobInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        dobInput = findViewById(R.id.dob_input)
        val emailVerify_text = findViewById<TextView>(R.id.verify_text)

        emailVerify_text.setOnClickListener{
            val intent =Intent(this,EmailOTPVerification::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
                    val cleanC = current.replace("[^\\d]".toRegex(), "")

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

        // Gender options to display in the dropdown
        val genderOptions = resources.getStringArray(R.array.gender_options)

        // Setting the adapter to the MaterialAutoCompleteTextView for gender dropdown
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        genderInput.setAdapter(adapter)

    }
}
