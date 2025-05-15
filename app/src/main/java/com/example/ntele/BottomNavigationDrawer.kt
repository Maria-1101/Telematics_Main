package com.example.ntele

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ntele.fragments.HomeFragment
import com.example.ntele.fragments.ServiceFragment
import com.example.ntele.fragments.VehicleFragment
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation


class BottomNavigationDrawer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_navigation_drawer)

        val bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)

        bottomNavigation.add(
            CurvedBottomNavigation.Model(1, "Service", R.drawable.service_icon)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(2, "Home", R.drawable.home)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(3, "My Vehicle", R.drawable.my_vehicle)
        )
        bottomNavigation.setOnClickMenuListener {
            when (it.id) {
                1 -> {
                    replaceFragment(ServiceFragment())
                }
                2 -> {
                    replaceFragment(HomeFragment())
                }
                3 -> {
                    replaceFragment(VehicleFragment())
                }
            }
        }
        //default screen selected
        replaceFragment(HomeFragment())
        bottomNavigation.show(2)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer,fragment)
            .commit()
    }
}
