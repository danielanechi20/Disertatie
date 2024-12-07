package com.example.licentaincercarea1

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

class SignInUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var signUpButton: Button
    private lateinit var googleSignInButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var toggleSignInSignUpTextView: TextView
    private lateinit var buttonSamsungFit: Button

    companion object {
        private const val TAG = "SignInUpActivity"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin_up)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("125847385009-de5hbj6ial4q4paq5r82bjk3o92aq147.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Force sign out to allow user to choose a different account
        googleSignInClient.signOut()

        // Initialize UI elements
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        signInButton = findViewById(R.id.buttonSignIn)
        signUpButton = findViewById(R.id.buttonSignUp)
        googleSignInButton = findViewById(R.id.buttonGoogleSignIn)
        forgotPasswordTextView = findViewById(R.id.textForgotPassword)
        toggleSignInSignUpTextView = findViewById(R.id.textToggleSignInSignUp)
        buttonSamsungFit = findViewById(R.id.buttonSamsungFit) // Samsung Health Connect Button

        // Set onClick listeners
        signInButton.setOnClickListener { signInWithEmail() }
        signUpButton.setOnClickListener { signUpWithEmail() }
        googleSignInButton.setOnClickListener { signInWithGoogle() }
        forgotPasswordTextView.setOnClickListener { resetPassword() }
        toggleSignInSignUpTextView.setOnClickListener { toggleSignInSignUp() }

        // Samsung Health Connect button listener
        buttonSamsungFit.setOnClickListener {
           // Start Samsung Health connection process
        }

        // Hide signUpButton initially (default is Sign In mode)
        signUpButton.visibility = Button.GONE
    }

    private fun toggleSignInSignUp() {
        if (signUpButton.visibility == Button.VISIBLE) {
            // Switch to Sign In mode
            signUpButton.visibility = Button.GONE
            signInButton.visibility = Button.VISIBLE
            googleSignInButton.visibility = Button.VISIBLE
            forgotPasswordTextView.visibility = TextView.VISIBLE
            toggleSignInSignUpTextView.text = "Don't have an account? Sign Up"
        } else {
            // Switch to Sign Up mode
            signUpButton.visibility = Button.VISIBLE
            signInButton.visibility = Button.GONE
            googleSignInButton.visibility = Button.GONE
            forgotPasswordTextView.visibility = TextView.GONE
            toggleSignInSignUpTextView.text = "Already have an account? Sign In"
        }
    }

    private fun signInWithEmail() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign In successful!", Toast.LENGTH_SHORT).show()
                    updateUI(auth.currentUser)
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> Toast.makeText(this, "Invalid email.", Toast.LENGTH_SHORT).show()
                        is FirebaseAuthInvalidCredentialsException -> Toast.makeText(this, "Invalid password.", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this, "Sign In failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun signUpWithEmail() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserDataToFirestore(auth.currentUser)
                    updateUI(auth.currentUser)
                } else {
                    Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        googleSignInClient.signOut()
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "Google Sign-In failed", e)
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserDataToFirestore(auth.currentUser)
                    updateUI(auth.currentUser)
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun resetPassword() {
        val email = emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserDataToFirestore(user: FirebaseUser?) {
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "email" to user?.email
        )
        user?.uid?.let {
            db.collection("users").document(it).set(data, SetOptions.merge())
                .addOnSuccessListener { Log.d(TAG, "User data successfully written to Firestore") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing user data to Firestore", e) }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MyAccountActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Samsung Health Integration


}
