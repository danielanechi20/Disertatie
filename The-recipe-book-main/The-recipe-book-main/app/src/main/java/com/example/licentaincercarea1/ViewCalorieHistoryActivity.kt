package com.example.licentaincercarea1

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ViewCalorieHistoryActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var calorieLineChart: LineChart
    private lateinit var timePeriodSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_calorie_history)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        calorieLineChart = findViewById(R.id.calorieLineChart)
        timePeriodSpinner = findViewById(R.id.timePeriodSpinner)

        // Set up the time period spinner
        timePeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val timePeriod = parent?.getItemAtPosition(position).toString()
                loadCalorieHistory(timePeriod)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Load default history (last week)
        loadCalorieHistory("Last Week")
    }

    // Fetch the calorie history based on the selected time period
    private fun loadCalorieHistory(timePeriod: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val timeFrame = getTimeFrameForPeriod(timePeriod)

            // Use lifecycleScope to bind coroutine to Activity lifecycle
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val documents = db.collection("users").document(user.uid).collection("calories")
                        .whereGreaterThanOrEqualTo("date", timeFrame.first)
                        .whereLessThanOrEqualTo("date", timeFrame.second)
                        .get()
                        .await()  // Await the Firebase query result

                    val calorieEntries = ArrayList<Entry>()
                    var index = 0f

                    for (document in documents) {
                        val calories = document.getLong("consumedCalories") ?: 0L
                        calorieEntries.add(Entry(index, calories.toFloat()))
                        index++
                    }

                    // Update the chart on the main thread
                    withContext(Dispatchers.Main) {
                        if (!isFinishing && !isDestroyed) {
                            updateChart(calorieEntries)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ViewCalorieHistory", "Error fetching calorie history", e)
                }
            }
        }
    }

    // Get the time frame based on the selected period (Last Week, Last Month, etc.)
    private fun getTimeFrameForPeriod(period: String): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        when (period) {
            "Last Week" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            "Last Month" -> calendar.add(Calendar.MONTH, -1)
            "Last 3 Months" -> calendar.add(Calendar.MONTH, -3)
        }
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return Pair(startDate, endDate)
    }

    // Update the chart with the fetched calorie data
    private fun updateChart(entries: ArrayList<Entry>) {
        // Ensure the activity is not destroyed
        if (!isFinishing && !isDestroyed) {
            // Create a dataset for calories
            val dataSet = LineDataSet(entries, "Calories Consumed")
            dataSet.lineWidth = 2.5f

            // Add a limit line for the calorie goal
            val calorieGoalLine = LimitLine(2000f, "Calorie Goal")
            calorieLineChart.axisLeft.addLimitLine(calorieGoalLine)

            // Set data to the chart
            val lineData = LineData(dataSet)
            calorieLineChart.data = lineData
            calorieLineChart.invalidate()  // Refresh the chart
        } else {
            Log.d("ViewCalorieHistory", "Activity is no longer active, not updating the chart.")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("ViewCalorieHistory", "Activity started")
    }

    override fun onStop() {
        super.onStop()
        Log.d("ViewCalorieHistory", "Activity stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ViewCalorieHistory", "Activity destroyed")
    }
}
