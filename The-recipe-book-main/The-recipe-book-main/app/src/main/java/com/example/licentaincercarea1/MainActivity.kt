package com.example.licentaincercarea1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.licentaincercarea1.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()  // Call the function to set button listeners
        FirebaseApp.initializeApp(this)
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)

    }

    // Function to set listeners for buttons
    private fun setListeners() {
        // Browse button (for general navigation)
        binding.buttonbrowse.setOnClickListener {
            startActivity(Intent(this@MainActivity, FragmentBase::class.java))
        }

        // Sign In button click listener
        binding.buttonsignin.setOnClickListener {
            // Navigate to the SignIn screen (assuming SignInActivity exists)
            val signInIntent = Intent(this@MainActivity, SignInUpActivity::class.java)
            startActivity(signInIntent)
        }


    }

}
