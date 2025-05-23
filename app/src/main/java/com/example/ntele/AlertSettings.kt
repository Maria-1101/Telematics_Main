package com.example.ntele

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ntele.fragments.VehicleFragment
import com.google.android.material.switchmaterial.SwitchMaterial

class AlertSettings : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var speedAlertSwitch: SwitchMaterial
    private lateinit var timeFenceSwitch: SwitchMaterial
    private lateinit var emergencyContactSwitch: SwitchMaterial
    private lateinit var socSwitch: SwitchMaterial
    private lateinit var speedAlertFrame: FrameLayout
    private lateinit var timeFenceFrame: FrameLayout
    private lateinit var emergencyContactFrame: FrameLayout
    private lateinit var socFrame: FrameLayout
    private lateinit var geofenceSwitch: SwitchMaterial
    private lateinit var geofenceFrame: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alert_settings)

        backButton = findViewById(R.id.back_button)
        speedAlertSwitch = findViewById(R.id.speed_alert_toggle)
        timeFenceSwitch =  findViewById(R.id.time_fence_toggle)
        emergencyContactSwitch = findViewById(R.id.emergency_contact_toggle)
        socSwitch = findViewById(R.id.soc_toggle)
        speedAlertFrame = findViewById(R.id.speed_alert_frame)
        timeFenceFrame = findViewById(R.id.time_fence_frame)
        emergencyContactFrame = findViewById(R.id.emergency_contact_frame)
        socFrame = findViewById(R.id.soc_frame)
        geofenceSwitch = findViewById(R.id.geofence_toggle)
        geofenceFrame =  findViewById(R.id.geofence_frame)

        // Set all frames to dimmed and non-clickable at the start
        speedAlertFrame.alpha = 0.5f
        speedAlertFrame.isClickable = false

        timeFenceFrame.alpha = 0.5f
        timeFenceFrame.isClickable = false

        emergencyContactFrame.alpha = 0.5f
        emergencyContactFrame.isClickable = false

        socFrame.alpha = 0.5f
        socFrame.isClickable = false

        geofenceFrame.alpha = 0.5f
        geofenceFrame.isClickable = false

        //Allowing frame selection is switch is checked
        speedAlertSwitch.setOnCheckedChangeListener { _, isChecked ->
            speedAlertFrame.alpha = if (isChecked) 1.0f else 0.5f
        }

        timeFenceSwitch.setOnCheckedChangeListener { _, isChecked ->
            timeFenceFrame.alpha = if (isChecked) 1.0f else 0.5f
        }

        emergencyContactSwitch.setOnCheckedChangeListener { _, isChecked ->
            emergencyContactFrame.alpha = if (isChecked) 1.0f else 0.5f
        }

        socSwitch.setOnCheckedChangeListener { _, isChecked ->
            socFrame.alpha = if (isChecked) 1.0f else 0.5f
        }

        geofenceSwitch.setOnCheckedChangeListener { _, isChecked ->
            geofenceFrame.alpha = if (isChecked) 1.0f else 0.5f
        }


        speedAlertFrame.setOnClickListener {
            if (!speedAlertSwitch.isChecked) {
                Toast.makeText(this, "Please turn on the Speed Alert switch.", Toast.LENGTH_SHORT).show()
            } else {
                val bottomSheet = OverlayFragment()
                bottomSheet.show(supportFragmentManager, "SpeedAlertBottomSheet")
            }
        }

        timeFenceFrame.setOnClickListener {
            if (!timeFenceSwitch.isChecked) {
                Toast.makeText(this, "Please turn on the Time Fence switch.", Toast.LENGTH_SHORT).show()
            } else {
                // Handle click
            }
        }

        emergencyContactFrame.setOnClickListener {
            if (!emergencyContactSwitch.isChecked) {
                Toast.makeText(this, "Please turn on the Emergency Contact switch.", Toast.LENGTH_SHORT).show()
            } else {
                // Handle click
            }
        }

        socFrame.setOnClickListener {
            if (!socSwitch.isChecked) {
                Toast.makeText(this, "Please turn on the SOC switch.", Toast.LENGTH_SHORT).show()
            } else {
                // Handle click
            }
        }

        backButton.setOnClickListener{
            val intent = Intent(this, BottomNavigationDrawer::class.java)
            intent.putExtra("openFragment", "vehicle")
            startActivity(intent)
            finish()
        }
    }
}