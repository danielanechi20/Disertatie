package com.example.licentaincercarea1

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.licentaincercarea1.databinding.ActivityMyaccountBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

class MyAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyaccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private var calorieGoal = 2000L

    private val addCaloriesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // No need to call fetchCaloriesConsumed here, as the snapshot listener will update automatically
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyaccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupCaloriesConsumedListener()
        fetchCalorieGoal() // Fetch the calorie goal when activity starts

        binding.addCaloriesButton.setOnClickListener {
            val intent = Intent(this, AddCaloriesActivity::class.java)
            addCaloriesLauncher.launch(intent)
        }

        binding.viewCaloriesHistoryButton.setOnClickListener {
            val intent = Intent(this, ViewCalorieHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.setCalorieGoalsButton.setOnClickListener {
            showSetGoalDialog()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signOutButton.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        auth.signOut()
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
                    calorieGoal = document.getLong("calorieGoal") ?: calorieGoal
                    updateProgressBar(binding.caloriesConsumedToday.text.toString().toLong())
                } else {
                    calorieGoal = 2000L // Default goal if none is set
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
        input.hint = calorieGoal.toString() // Show current goal as hint
        dialog.setView(input)

        dialog.setPositiveButton("Save") { _, _ ->
            val inputText = input.text.toString()
            if (inputText.isNotEmpty()) {
                val newGoal = inputText.toLongOrNull()
                if (newGoal != null && newGoal > 0) {
                    saveCalorieGoal(newGoal)
                } else {
                    Toast.makeText(this, "Please enter a valid calorie goal", Toast.LENGTH_SHORT).show()
                }
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
                    this.calorieGoal = calorieGoal // Update local variable
                    updateProgressBar(binding.caloriesConsumedToday.text.toString().toLong())
                    Toast.makeText(this, "Calorie goal updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update calorie goal", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
