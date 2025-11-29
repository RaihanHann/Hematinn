package com.example.catatankeuangan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    
    private lateinit var tvGoalsName: TextView
    private lateinit var tvGoalsSummary: TextView
    private lateinit var tvGoalDeadline: TextView
    private lateinit var progressBarGoals: ProgressBar
    private lateinit var btnAddTransaction: Button
    private lateinit var llGoalSection: LinearLayout
    private lateinit var btnHistoryPemasukan: Button
    private lateinit var btnHistoryPengeluaran: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        tvGoalsName = findViewById(R.id.tv_goals_name)
        tvGoalsSummary = findViewById(R.id.tv_goals_summary)
        tvGoalDeadline = findViewById(R.id.tv_goal_deadline)
        progressBarGoals = findViewById(R.id.progress_bar_goals)
        btnAddTransaction = findViewById(R.id.btn_add_transaction)
        llGoalSection = findViewById(R.id.ll_goal_section)
        btnHistoryPemasukan = findViewById(R.id.btn_history_pemasukan)
        btnHistoryPengeluaran = findViewById(R.id.btn_history_pengeluaran)

        loadGoalsData()

        btnAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        llGoalSection.setOnClickListener {
            val intent = Intent(this, EditGoalActivity::class.java)
            startActivity(intent)
        }

        btnHistoryPemasukan.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("TRANSACTION_TYPE", "Pemasukan")
            startActivity(intent)
        }

        btnHistoryPengeluaran.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("TRANSACTION_TYPE", "Pengeluaran")
            startActivity(intent)
        }
    }
    
    private fun loadGoalsData() {
        val goalsData = dbHelper.getGoals()

        if (goalsData != null) {
            val nama = goalsData["nama"] as String
            val target = goalsData["target"] as Double
            val terkumpul = goalsData["terkumpul"] as Double
            val deadline = goalsData["deadline"] as String

            val persentase: Int = if (target > 0) {
                ((terkumpul / target) * 100).toInt()
            } else {
                0
            }

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            val terkumpulFormatted = formatRupiah.format(terkumpul)
            val targetFormatted = formatRupiah.format(target)
            
            tvGoalsName.text = nama
            tvGoalsSummary.text = "$terkumpulFormatted / $targetFormatted ($persentase%)"
            progressBarGoals.progress = persentase

            if (deadline.isNotEmpty()) {
                tvGoalDeadline.text = "Deadline: $deadline"
            } else {
                tvGoalDeadline.text = "Deadline: Belum diatur"
            }
        } else {
            tvGoalsName.text = "Goals Belum Ditetapkan"
            tvGoalDeadline.text = "Deadline: Belum diatur"
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadGoalsData()
    }
}