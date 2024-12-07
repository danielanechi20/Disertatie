package com.example.licentaincercarea1

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.licentaincercarea1.databinding.ActivityMyaccountBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyaccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var calorieGoal = 2000L

    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyaccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        requestGoogleFitPermissions()
        setupCaloriesConsumedListener()
        fetchCalorieGoal()

        binding.addCaloriesButton.setOnClickListener {
            val intent = Intent(this, AddCaloriesActivity::class.java)
            startActivity(intent)
        }

        binding.viewCaloriesHistoryButton.setOnClickListener {
            val intent = Intent(this, ViewCalorieHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.setCalorieGoalsButton.setOnClickListener {
            showSetGoalDialog()
        }

        binding.signOutButton.setOnClickListener {
            signOut()
        }
    }

    private fun requestGoogleFitPermissions() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_NUTRITION_SUMMARY, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            accessGoogleFit()
        }
    }

    private fun accessGoogleFit() {
        readDailyNutritionData()
        Log.i("MyAccountActivity", "Google Fit permissions granted.")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                accessGoogleFit()
            } else {
                Toast.makeText(this, "Google Fit permissions denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readDailyNutritionData() {
        val end = Calendar.getInstance().timeInMillis
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1) // Get data for the last 24 hours
        }.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_NUTRITION)
            .setTimeRange(start, end, TimeUnit.MILLISECONDS)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, FitnessOptions.builder().build())

        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                var totalCalories = 0f
                for (dataSet in response.dataSets) {
                    for (dataPoint in dataSet.dataPoints) {
                        val nutrients = dataPoint.getValue(Field.FIELD_NUTRIENTS)
                        val calories = nutrients?.getKeyValue(Field.NUTRIENT_CALORIES) ?: 0f
                        totalCalories += calories
                    }
                }

                // Display the calories from Google Fit
                binding.caloriesConsumedToday.text = totalCalories.toString()
                updateProgressBar(totalCalories.toLong())

                // Save the calories to Firebase, replacing the existing value
                saveCaloriesToFirebase(totalCalories.toLong())
            }
            .addOnFailureListener { e ->
                Log.e("MyAccountActivity", "Failed to read daily nutrition data: ${e.message}")
                Toast.makeText(this, "Failed to read daily nutrition data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveCaloriesToFirebase(calories: Long) {
        val currentUser = auth.currentUser
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(Date())

        currentUser?.let { user ->
            val docRef = db.collection("users").document(user.uid).collection("calories").document(todayDate)
            val data = mapOf("consumedCalories" to calories)

            docRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("MyAccountActivity", "Calories updated in Firebase successfully.")
                    Toast.makeText(this, "Calories synced with Firebase.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("MyAccountActivity", "Failed to update calories in Firebase: ${e.message}")
                    Toast.makeText(this, "Failed to sync calories with Firebase.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun signOut() {
        auth.signOut()
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut().addOnCompleteListener(this) {
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignInUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupCaloriesConsumedListener() {
        val currentUser = auth.currentUser
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = sdf.format(Date())

        currentUser?.let { user ->
            val docRef = db.collection("users").document(user.uid).collection("calories").document(todayDate)
            docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MyAccountActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val consumedCalories = snapshot.getLong("consumedCalories") ?: 0L
                    binding.caloriesConsumedToday.text = consumedCalories.toString()
                    updateProgressBar(consumedCalories)
                } else {
                    binding.caloriesConsumedToday.text = "0"
                    updateProgressBar(0)
                }
            }
        }
    }

    private fun fetchCalorieGoal() {
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            val docRef = db.collection("users").document(user.uid)
            docRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val calorieGoalValue = document.getDouble("calorieGoal") ?: calorieGoal.toDouble()
                    calorieGoal = calorieGoalValue.toLong()
                    updateProgressBar(binding.caloriesConsumedToday.text.toString().toLongOrNull() ?: 0L)
                } else {
                    calorieGoal = 2000L
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch calorie goal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProgressBar(consumedCalories: Long) {
        val progressBar = findViewById<CircularGradientProgressBar>(R.id.calorieProgressBar)
        progressBar.max = (calorieGoal + 1000).toInt()
        progressBar.progress = consumedCalories.toInt()
    }

    private fun showSetGoalDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Set Calorie Goal")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = calorieGoal.toString()
        dialog.setView(input)

        dialog.setPositiveButton("Save") { _, _ ->
            val inputText = input.text.toString()
            val newGoal = inputText.toLongOrNull()
            if (newGoal != null && newGoal > 0) {
                saveCalorieGoal(newGoal)
            } else {
                Toast.makeText(this, "Please enter a valid calorie goal", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
        dialog.show()
    }
    
     private fun saveCalorieGoal(calorieGoal: Long) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .set(mapOf("calorieGoal" to calorieGoal), SetOptions.merge())
                .addOnSuccessListener {
                    this.calorieGoal = calorieGoal
                    updateProgressBar(binding.caloriesConsumedToday.text.toString().toLongOrNull() ?: 0L)
                    Toast.makeText(this, "Calorie goal updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update calorie goal", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
