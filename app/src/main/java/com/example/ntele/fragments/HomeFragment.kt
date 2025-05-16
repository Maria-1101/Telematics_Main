package com.example.ntele.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ntele.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private val apiKey = "e06221ba81ed6222afad994d485bc10a" // your Mappls API key

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapImageView = rootView.findViewById<ImageView>(R.id.map_image)

        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission granted, get location
            getCurrentLocation { latitude, longitude ->
                // Build Mappls Static Map URL with current location
                val mapUrl =
                    "https://apis.mappls.com/advancedmaps/v1/$apiKey/still_image?center=$latitude,$longitude&zoom=15&size=600x300"

                // Load image with Glide
                Glide.with(this)
                    .load(mapUrl)
                    .into(mapImageView)
            }
        }

        return rootView
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getCurrentLocation { latitude, longitude ->
                    val mapImageView = view?.findViewById<ImageView>(R.id.map_image) ?: return@getCurrentLocation

                    val mapUrl =
                        "https://apis.mappls.com/advancedmaps/v1/$apiKey/still_image?center=$latitude,$longitude&zoom=15&size=600x300"

                    Glide.with(this)
                        .load(mapUrl)
                        .into(mapImageView)
                }
            }
        }
    }

    private fun getCurrentLocation(onLocationFetched: (Double, Double) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onLocationFetched(location.latitude, location.longitude)
                    } else {
                        // Location is null, maybe request updates or notify the user
                    }
                }
                .addOnFailureListener {
                    // Handle location fetch failure
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Handle permission error (although unlikely here due to checks)
        }
    }

}
