package com.example.licentaincercarea1

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.licentaincercarea1.databinding.ActivityAddcaloriesBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Date())
        binding.dateTextView.text = selectedDate

        binding.selectDateButton.setOnClickListener { showDatePicker() }

        binding.saveCaloriesButton.setOnClickListener {
            val newCaloriesStr = binding.caloriesInput.text.toString()
            if (newCaloriesStr.isNotEmpty()) {
                val newCalories = newCaloriesStr.toFloat()
                saveCaloriesToFirestore(newCalories)
            } else {
                Toast.makeText(this, "Please enter a calorie amount.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun saveCaloriesToFirestore(newCalories: Float) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val docRef = db.collection("users").document(user.uid).collection("calories").document(selectedDate)
            docRef.get().addOnSuccessListener { document ->
                val updatedCalories = if (document.exists()) {
                    val currentCalories = document.getLong("consumedCalories") ?: 0L
                    currentCalories + newCalories.toDouble()
                } else {
                    newCalories.toDouble()
                }

                val calorieData = hashMapOf("consumedCalories" to updatedCalories)
                docRef.set(calorieData, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Calories added successfully!", Toast.LENGTH_SHORT).show()
                        val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
                        if (googleAccount != null) {
                            syncWithGoogleFitNutrition(newCalories)
                        } else {
                            Toast.makeText(this, "Not signed in with Google. Skipping Google Fit sync.", Toast.LENGTH_SHORT).show()
                        }
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("AddCaloriesActivity", "Failed to update calories: ${e.message}")
                        Toast.makeText(this, "Failed to update calories: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun syncWithGoogleFitNutrition(newCalories: Float) {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_WRITE)
            .addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            val nutritionSource = DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_NUTRITION)
                .setType(DataSource.TYPE_RAW)
                .build()

            val timestamp = System.currentTimeMillis()

            val nutrients = mapOf(
                Field.NUTRIENT_CALORIES to newCalories
            )

            val dataPoint = DataPoint.builder(nutritionSource)
                .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
                .setField(Field.FIELD_FOOD_ITEM, "Custom Entry") // Placeholder for food item
                .setField(Field.FIELD_MEAL_TYPE, Field.MEAL_TYPE_UNKNOWN) // Default meal type
                .setField(Field.FIELD_NUTRIENTS, nutrients)
                .build()

            val dataSet = DataSet.create(nutritionSource).apply {
                add(dataPoint)
            }

            Fitness.getHistoryClient(this, account)
                .insertData(dataSet)
                .addOnSuccessListener {
                    Toast.makeText(this, "Synced with Google Fit Nutrition successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("AddCaloriesActivity", "Failed to sync with Google Fit: ${e.message}")
                    Toast.makeText(this, "Failed to sync with Google Fit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            GoogleSignIn.requestPermissions(
                this,
                1001,
                account,
                fitnessOptions
            )
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
            binding.dateTextView.text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }
}
