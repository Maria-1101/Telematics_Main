package com.example.ntele.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.ntele.AboutVehicle
import com.example.ntele.AlertSettings
import com.example.ntele.R

class VehicleFragment : Fragment() {

    private lateinit var alertFrame: FrameLayout
    private lateinit var NotificationFrame: FrameLayout
    private lateinit var DriverAnalyticsFrame: FrameLayout
    private lateinit var TpmsFrame: FrameLayout
    private lateinit var AboutVehicleFrame: FrameLayout
    private lateinit var EventLogFrame: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_vehicle, container, false)
        alertFrame = rootView.findViewById(R.id.alert_settings_frame)
        NotificationFrame = rootView.findViewById(R.id.notification_frame)
        DriverAnalyticsFrame = rootView.findViewById(R.id.driver_analytics_frame)
        TpmsFrame = rootView.findViewById(R.id.tpms_frame)
        AboutVehicleFrame = rootView.findViewById(R.id.about_vehicle_frame)
        EventLogFrame = rootView.findViewById(R.id.event_logs_frame)

        alertFrame.setOnClickListener {
            val intent = Intent(requireContext(), AlertSettings::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return rootView
    }
}