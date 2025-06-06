package com.example.ntele

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ProfileFragment : Fragment() {

    private lateinit var logoutBtn: ImageButton
    private lateinit var closeBtn: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logoutBtn = view.findViewById(R.id.logout_btn)
        closeBtn = view.findViewById(R.id.close_btn)

        logoutBtn.setOnClickListener {
            val prefs =
                requireContext().getSharedPreferences("UserSession", AppCompatActivity.MODE_PRIVATE)
            prefs.edit().clear().apply()
            val intent = Intent(requireContext(), LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        closeBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()

        }
    }
}
