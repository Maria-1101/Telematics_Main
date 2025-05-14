package com.example.ntele

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class UserDetails : AppCompatActivity() {
    private lateinit var dobInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        dobInput = findViewById(R.id.dob_input)  // <- Correct ID of the EditText

        dobInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    dobInput.setText(dob)
                },
                year, month, day
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
        val genderInput: MaterialAutoCompleteTextView = findViewById(R.id.gender_input)

        // Gender options to display in the dropdown
        val genderOptions = resources.getStringArray(R.array.gender_options)

        // Setting the adapter to the MaterialAutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        genderInput.setAdapter(adapter)
    }
}
