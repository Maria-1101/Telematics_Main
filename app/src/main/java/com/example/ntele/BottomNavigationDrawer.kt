package com.example.ntele

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ntele.databinding.ActivityBottomNavigationDrawerBinding
import com.example.ntele.fragments.HomeFragment
import com.example.ntele.fragments.ServiceFragment
import com.example.ntele.fragments.VehicleFragment

class BottomNavigationDrawer : AppCompatActivity() {

    private lateinit var binding: ActivityBottomNavigationDrawerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigationDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Default Fragment
        replaceFragment(HomeFragment())

        binding.customNav.setOnItemSelectedListener { index ->
            when (index) {
                0 -> replaceFragment(HomeFragment())
                1 -> replaceFragment(VehicleFragment())
                2 -> replaceFragment(ServiceFragment())
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
