package com.example.ntele.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ntele.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.switchmaterial.SwitchMaterial

class HomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val apiKey = "20b2c7a2d61fa8e3c0f04cc4c3f5cbb3" // Mappls API key
    private lateinit var seatToggleBtn: SwitchMaterial
    private lateinit var vehicleToggleBtn: SwitchMaterial
    private lateinit var handleToggleBtn: SwitchMaterial
    private lateinit var immobilizationToggleBtn: SwitchMaterial
    private lateinit var sosButtonDefault: ImageButton
    private lateinit var sosButtonSelected: ImageButton
    private lateinit var sosFrame: FrameLayout
    private lateinit var sosText: TextView

    private var mapImageView: ImageView? = null
    var isSelected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapImageView = rootView.findViewById(R.id.map_image)
        seatToggleBtn = rootView.findViewById(R.id.seat_lock_toggle)
        vehicleToggleBtn = rootView.findViewById(R.id.vehicle_lock_toggle)
        handleToggleBtn =  rootView.findViewById(R.id.handle_lock_toggle)
        immobilizationToggleBtn =  rootView.findViewById(R.id.immobilization_toggle)
        sosButtonDefault = rootView.findViewById(R.id.sos_button_default)
        sosButtonSelected = rootView.findViewById(R.id.sos_button_selected)
        sosFrame = rootView.findViewById(R.id.sos_frame)
        sosText =  rootView.findViewById(R.id.sos_text)

        sosButtonDefault.setOnClickListener {
            sosFrame.setBackgroundResource(R.drawable.selected_sos_bg)
            sosButtonDefault.visibility = View.GONE
            sosButtonSelected.visibility = View.VISIBLE
            val boldTypeface = ResourcesCompat.getFont(requireContext(), R.font.roboto_bold)
            sosText.typeface = boldTypeface
        }

        sosButtonSelected.setOnClickListener {
            sosFrame.setBackgroundResource(R.drawable.sos_bg) // your original bg drawable
            sosButtonDefault.visibility = View.VISIBLE
            sosButtonSelected.visibility = View.GONE
            val regularTypeface = ResourcesCompat.getFont(requireContext(), R.font.roboto_regular)
            sosText.typeface = regularTypeface
        }


        seatToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(), "Switch On", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Switch Off", Toast.LENGTH_SHORT).show()
            }
        }

        immobilizationToggleBtn.isEnabled = false
        val overlay = rootView.findViewById<View>(R.id.immobilization_click_overlay)

        overlay.setOnClickListener {
            Toast.makeText(requireContext(), "Requires premium subscription", Toast.LENGTH_SHORT).show()

        }


        if (checkLocationPermission()) {
            fetchAndUpdateMap()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (checkLocationPermission()) {
            fetchAndUpdateMap()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchAndUpdateMap()
        }
    }

    private fun fetchAndUpdateMap() {
        getCurrentLocation { latitude, longitude ->
            updateMapImage(latitude, longitude)
        }
    }

    private fun getCurrentLocation(onLocationFetched: (Double, Double) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        onLocationFetched(it.latitude, it.longitude)
                    }
                }
                .addOnFailureListener {
                    // Handle failure
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun updateMapImage(latitude: Double, longitude: Double) {
        mapImageView?.post {
            val widthPx = mapImageView!!.width
            val heightPx = mapImageView!!.height

            if (widthPx == 0 || heightPx == 0) {
                // fallback if size not determined
                return@post
            }

            val mapUrl = "https://apis.mappls.com/advancedmaps/v1/$apiKey/still_image" +
                    "?center=$latitude,$longitude" +
                    "&zoom=15" +
                    "&size=${widthPx}x${heightPx}" +
                    "&pt=$latitude,$longitude,bluedot" +
                    "&format=png&scale=2"

            val cornerRadiusDp = 16
            val scale = resources.displayMetrics.density
            val cornerRadiusPx = (cornerRadiusDp * scale + 0.5f).toInt()

            Glide.with(this)
                .load(mapUrl)
                .apply(
                    RequestOptions()
                        .override(widthPx, heightPx)
                        .transform(RoundedCorners(cornerRadiusPx))
                )
                .into(mapImageView!!)
        }
    }
}
