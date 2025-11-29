package com.example.catatankeuangan

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

class EditGoalActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var etGoalName: EditText
    private lateinit var etTargetAmount: EditText
    private lateinit var tvDeadline: TextView
    private lateinit var btnPickDate: Button
    private lateinit var btnSaveChanges: Button

    private var deadline: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_goal)

        dbHelper = DBHelper(this)

        etGoalName = findViewById(R.id.et_goal_name)
        etTargetAmount = findViewById(R.id.et_target_amount)
        tvDeadline = findViewById(R.id.tv_deadline)
        btnPickDate = findViewById(R.id.btn_pick_date)
        btnSaveChanges = findViewById(R.id.btn_save_changes)

        loadCurrentGoal()

        btnPickDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadCurrentGoal() {
        val currentGoal = dbHelper.getGoals()
        if (currentGoal != null) {
            etGoalName.setText(currentGoal["nama"] as? String)
            etTargetAmount.setText((currentGoal["target"] as? Double).toString())
            deadline = currentGoal["deadline"] as? String ?: ""
            if (deadline.isNotEmpty()) {
                tvDeadline.text = deadline
            } else {
                tvDeadline.text = "Belum diatur"
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                tvDeadline.text = selectedDate
                deadline = selectedDate
            }, year, month, day)
        datePickerDialog.show()
    }

    private fun saveChanges() {
        val goalName = etGoalName.text.toString().trim()
        val targetAmountText = etTargetAmount.text.toString().trim()

        if (goalName.isEmpty() || targetAmountText.isEmpty()) {
            Toast.makeText(this, "Nama tujuan dan target jumlah tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val targetAmount = targetAmountText.toDoubleOrNull()
        if (targetAmount == null || targetAmount <= 0) {
            Toast.makeText(this, "Target jumlah harus berupa angka positif.", Toast.LENGTH_SHORT).show()
            return
        }

        val success = dbHelper.updateGoal(goalName, targetAmount, deadline)

        if (success) {
            Toast.makeText(this, "Tujuan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal memperbarui tujuan.", Toast.LENGTH_SHORT).show()
        }
    }
}