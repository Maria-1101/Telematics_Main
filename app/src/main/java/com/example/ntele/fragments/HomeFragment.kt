package com.example.ntele.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Outline
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ntele.ProfileFragment
import com.example.ntele.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.*
import com.mappls.sdk.maps.MapView
import com.mappls.sdk.maps.MapplsMap
import com.mappls.sdk.maps.OnMapReadyCallback
import com.mappls.sdk.maps.geometry.LatLng
import com.mappls.sdk.maps.location.LocationComponent
import com.mappls.sdk.maps.location.LocationComponentActivationOptions
import com.mappls.sdk.maps.location.modes.CameraMode
import com.mappls.sdk.maps.camera.CameraUpdateFactory
import com.mappls.sdk.maps.annotations.MarkerOptions


class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var seatToggleBtn: SwitchMaterial
    private lateinit var vehicleToggleBtn: SwitchMaterial
    private lateinit var handleToggleBtn: SwitchMaterial
    private lateinit var immobilizationToggleBtn: SwitchMaterial
    private lateinit var sosButtonDefault: ImageButton
    private lateinit var sosButtonSelected: ImageButton
    private lateinit var sosFrame: FrameLayout
    private lateinit var sosText: TextView
    private lateinit var nameField: TextView

    private lateinit var batteryLevel: TextView
    private lateinit var dteProgress: TextView
    private lateinit var parkedLocation: TextView
    private lateinit var parkedTime: TextView
    private lateinit var mileageInput: TextView
    private lateinit var speedInput: TextView
    private lateinit var co2Input: TextView
    private lateinit var distanceInput: TextView
    private lateinit var profileButton: ShapeableImageView
    private lateinit var batteryProgressBar: ProgressBar

    private lateinit var databaseRef: DatabaseReference
    private lateinit var mapView: MapView
    private var mapplsMap: MapplsMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapView = rootView.findViewById(R.id.map_image)
        seatToggleBtn = rootView.findViewById(R.id.seat_lock_toggle)
        vehicleToggleBtn = rootView.findViewById(R.id.vehicle_lock_toggle)
        handleToggleBtn = rootView.findViewById(R.id.handle_lock_toggle)
        immobilizationToggleBtn = rootView.findViewById(R.id.immobilization_toggle)
        sosButtonDefault = rootView.findViewById(R.id.sos_button_default)
        sosButtonSelected = rootView.findViewById(R.id.sos_button_selected)
        sosFrame = rootView.findViewById(R.id.sos_frame)
        sosText = rootView.findViewById(R.id.sos_text)
        batteryLevel = rootView.findViewById(R.id.battery_level_detection_textview)
        dteProgress = rootView.findViewById(R.id.DTE_progress_textview)
        parkedLocation = rootView.findViewById(R.id.parked_place_textview)
        parkedTime = rootView.findViewById(R.id.parked_time_textview)
        mileageInput = rootView.findViewById(R.id.mileage_display_textview)
        speedInput = rootView.findViewById(R.id.speed_display_textview)
        co2Input = rootView.findViewById(R.id.co2_display_textview)
        distanceInput = rootView.findViewById(R.id.distance_display_textiew)
        nameField = rootView.findViewById(R.id.name_text)
        profileButton = rootView.findViewById(R.id.profile_image)
        batteryProgressBar = rootView.findViewById(R.id.battery_progress_bar)

        databaseRef = FirebaseDatabase.getInstance("https://telematics-a0e1f-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .reference

        // MapView initialization
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        //sharing name of logged in/ registered user to different screens in order to display in home fragment
        val sharedName = getUserNameFromPrefs()
        nameField.text = sharedName
        Log.d("name", "" + sharedName)

        loadDataFromFirebase(sharedName)

        //profile button click overlay from right side
        profileButton.setOnClickListener {
            val fragment = ProfileFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_right
                )
                .add(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit()
        }

        sosButtonDefault.setOnClickListener {
            updateSosState(1, sharedName)
        }

        sosButtonSelected.setOnClickListener {
            updateSosState(0, sharedName)
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

    // as the fragment is defined as a callback this method is called when the mappls map is ready
    override fun onMapReady(map: MapplsMap) {
        mapplsMap = map
        enableLocationFeatures()
        setupMapGestures()

        // Add rounded corners to the interactive MapView
        val cornerRadiusDp = 16 // Adjust as needed
        val cornerRadiusPx = (cornerRadiusDp * resources.displayMetrics.density).toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mapView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, cornerRadiusPx.toFloat())
                }
            }
            mapView.clipToOutline = true
        }
    }

    override fun onMapError(errorCode: Int, errorMessage: String?) {
        Toast.makeText(requireContext(), "Map error: $errorMessage ($errorCode)", Toast.LENGTH_LONG).show()
    }


    //this method activated the location tracking and display
    private fun enableLocationFeatures() {
        mapplsMap?.getStyle { style ->
            val locationComponent = mapplsMap?.locationComponent ?: return@getStyle

            if (!locationComponent.isLocationComponentActivated) {
                val activationOptions = LocationComponentActivationOptions.builder(requireContext(), style)
                    .useDefaultLocationEngine(true)
                    .build()

                locationComponent.activateLocationComponent(activationOptions)
            }


            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@getStyle
            }

            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
        }
    }


    //enables user interaction like zoom in/out etc
    private fun setupMapGestures() {
        mapplsMap?.uiSettings?.apply {
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = true
            isCompassEnabled = true
            isAttributionEnabled = false
            isLogoEnabled = false
        }

        mapplsMap?.animateCamera(
            CameraUpdateFactory.zoomTo(15.0),  // Smooth zoom level
            1000  // duration in ms
        )
    }

    private fun getUserNameFromPrefs(): String {
        val preferences = requireActivity().getSharedPreferences("UserSession", AppCompatActivity.MODE_PRIVATE)
        return preferences.getString("username", "User") ?: "User"
    }

    private fun updateSosState(state: Int, name: String?) {
        if (name == null) return
        databaseRef.child("HomeFragment").child(name).child("sos").setValue(state)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (state == 1) {
                        sosFrame.setBackgroundResource(R.drawable.selected_sos_bg)
                        sosButtonDefault.visibility = View.GONE
                        sosButtonSelected.visibility = View.VISIBLE
                        val boldTypeface = ResourcesCompat.getFont(requireContext(), R.font.roboto_bold)
                        sosText.typeface = boldTypeface
                        Toast.makeText(requireContext(), "SOS Activated", Toast.LENGTH_SHORT).show()
                    } else {
                        sosFrame.setBackgroundResource(R.drawable.sos_bg)
                        sosButtonDefault.visibility = View.VISIBLE
                        sosButtonSelected.visibility = View.GONE
                        val regularTypeface = ResourcesCompat.getFont(requireContext(), R.font.roboto_regular)
                        sosText.typeface = regularTypeface
                        Toast.makeText(requireContext(), "SOS Deactivated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to update SOS state", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadDataFromFirebase(userName: String) {
        val userRef = databaseRef.child("HomeFragment").child(userName)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ctx = context ?: return

                if (snapshot.exists()) {
                    val batteryVal = snapshot.child("battery_level").getValue(Int::class.java)
                    if (batteryVal != null) {
                        batteryLevel.text = "$batteryVal %"
                        batteryProgressBar.progress = batteryVal
                    } else {
                        batteryLevel.text = "-"
                        batteryProgressBar.progress = 0
                    }
                    dteProgress.text = snapshot.child("dte_progress").getValue(Int::class.java)?.let {
                        "DTE $it km"
                    } ?: "-"
                    mileageInput.text = snapshot.child("mileage").getValue(Double::class.java)?.let {
                        if (it % 1.0 == 0.0) {
                            "${it.toInt()} km"
                        } else {
                            String.format("%.2f km", it)
                        }
                    } ?: "-"
                    speedInput.text = snapshot.child("avg_speed").getValue(Double::class.java)?.let {
                        if (it % 1.0 == 0.0) {
                            "${it.toInt()} km/h"
                        } else {
                            String.format("%.2f km/h", it)
                        }
                    } ?: "-"
                    distanceInput.text = snapshot.child("total_distance").getValue(Double::class.java)?.let {
                        if (it % 1.0 == 0.0) {
                            "${it.toInt()} km"
                        } else {
                            String.format("%.2f km", it)
                        }
                    } ?: "-"
                    co2Input.text = snapshot.child("co2_saved").getValue(Double::class.java)?.let {
                        if (it % 1.0 == 0.0) {
                            "${it.toInt()} kg"
                        } else {
                            String.format("%.2f kg", it)
                        }
                    } ?: "-"
                    parkedLocation.text = snapshot.child("parked_place").getValue(String::class.java) ?: "-"
                    val parkedTimeRaw = snapshot.child("parked_time").getValue(Double::class.java)
                    parkedTime.text = parkedTimeRaw?.let { formatParkedTime(it) } ?: "-"
                    seatToggleBtn.isChecked = snapshot.child("seat_lock").getValue(Int::class.java) == 1
                    vehicleToggleBtn.isChecked = snapshot.child("vehicle_lock").getValue(Int::class.java) == 1
                    handleToggleBtn.isChecked = snapshot.child("handle_lock").getValue(Int::class.java) == 1
                    val sosState = snapshot.child("sos").getValue(Int::class.java) ?: 0
                    if (sosState == 1) {
                        sosFrame.setBackgroundResource(R.drawable.selected_sos_bg)
                        sosButtonDefault.visibility = View.GONE
                        sosButtonSelected.visibility = View.VISIBLE
                        val boldTypeface = ResourcesCompat.getFont(ctx, R.font.roboto_bold)
                        sosText.typeface = boldTypeface
                    } else {
                        sosFrame.setBackgroundResource(R.drawable.sos_bg)
                        sosButtonDefault.visibility = View.VISIBLE
                        sosButtonSelected.visibility = View.GONE
                        val regularTypeface = ResourcesCompat.getFont(ctx, R.font.roboto_regular)
                        sosText.typeface = regularTypeface
                    }
                } else {
                    Toast.makeText(requireContext(), "No data found for $userName", Toast.LENGTH_SHORT).show()
                }
            }

            private fun formatParkedTime(timeInMinutes: Double): String {
                val totalMinutes = timeInMinutes.toInt()
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                return "${hours}h ${minutes}m"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        seatToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            userRef.child("seat_lock").setValue(if (isChecked) 1 else 0)
            context?.let {
                Toast.makeText(requireContext(), if (isChecked) "Seat Lock Enabled" else "Seat Lock Disabled", Toast.LENGTH_SHORT).show()
            }
        }
        vehicleToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            userRef.child("vehicle_lock").setValue(if (isChecked) 1 else 0)
            context?.let {
                Toast.makeText(requireContext(), if (isChecked) "Vehicle Lock Enabled" else "Vehicle Lock Disabled", Toast.LENGTH_SHORT).show()
            }
        }
        handleToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            userRef.child("handle_lock").setValue(if (isChecked) 1 else 0)
            context?.let {
                Toast.makeText(requireContext(), if (isChecked) "Handle Lock Enabled" else "Handle Lock Disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatParkedTime(timeString: String): String {
        return try {
            val totalMinutes = timeString.toInt()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            "${hours}h ${minutes}m"
        } catch (e: NumberFormatException) {
            timeString
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (checkLocationPermission()) {
            fetchAndUpdateMap()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
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
            mapplsMap?.let { map ->
                val position = LatLng(latitude, longitude)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(position, 15.0)
                )
            }
        }
    }

    private fun getCurrentLocation(onLocationFetched: (Double, Double) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        onLocationFetched(it.latitude, it.longitude)

                        // Optional: Add marker
                        mapplsMap?.clear()  // Clear old markers
                        mapplsMap?.addMarker(
                            com.mappls.sdk.maps.annotations.MarkerOptions()
                                .position(latLng)
                                .title("You're here")
                                .snippet("Current Location")
                        )

                        mapplsMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15.0), 1000
                        )
                    }
                }
                .addOnFailureListener {
                    // Handle failure
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
