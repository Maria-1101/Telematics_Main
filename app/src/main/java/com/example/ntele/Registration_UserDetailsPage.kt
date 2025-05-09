package com.example.ntele

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Registration_UserDetailsPage : AppCompatActivity() {
    private lateinit var dobInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details)

        dobInput = findViewById(R.id.dob_input_layout)

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

            // Optional: Restrict future dates
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            datePickerDialog.show()
        }
    }
}
