package com.example.licentaincercarea1

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.licentaincercarea1.databinding.ActivityAddcaloriesBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class AddCaloriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddcaloriesBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddcaloriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set initial selected date to today's date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Date())
        binding.dateTextView.text = selectedDate

        binding.selectDateButton.setOnClickListener { showDatePicker() }

        binding.saveCaloriesButton.setOnClickListener {
            val newCaloriesStr = binding.caloriesInput.text.toString()
            if (newCaloriesStr.isNotEmpty()) {
                val newCalories = newCaloriesStr.toLong()
                val currentUser = auth.currentUser

                currentUser?.let { user ->
                    // Reference to the specific date document in Firestore for the logged-in user
                    val docRef = db.collection("users").document(user.uid).collection("calories").document(selectedDate)

                    docRef.get().addOnSuccessListener { document ->
                        val updatedCalories = if (document.exists()) {
                            // Document exists, add new calories to the current value
                            val currentCalories = document.getLong("consumedCalories") ?: 0L
                            currentCalories + newCalories
                        } else {
                            // New day, start from 0
                            newCalories
                        }

                        // Update or set the new calorie count in Firestore
                        val calorieData = hashMapOf("consumedCalories" to updatedCalories)
                        docRef.set(calorieData, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(this, "Calories added successfully!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e("AddCaloriesActivity", "Failed to update calories: ${e.message}")
                                Toast.makeText(this, "Failed to update calories: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a calorie amount.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedMonth = String.format("%02d", selectedMonth + 1)
            val formattedDay = String.format("%02d", selectedDay)
            selectedDate = "$selectedYear-$formattedMonth-$formattedDay"
            binding.dateTextView.text = selectedDate // Update displayed date to the selected date
        }, year, month, day)

        datePickerDialog.show()
    }
}
