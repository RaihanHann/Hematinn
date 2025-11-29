package com.example.catatankeuangan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.text.NumberFormat
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var lvHistory: ListView
    private lateinit var tvTitle: TextView
    private lateinit var transactionList: List<Transaction>
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        dbHelper = DBHelper(this)
        lvHistory = findViewById(R.id.lv_history)
        tvTitle = findViewById(R.id.tv_history_title)

        type = intent.getStringExtra("TRANSACTION_TYPE") ?: "Pemasukan"

        tvTitle.text = "Riwayat $type"
        title = "Riwayat $type"

        lvHistory.setOnItemLongClickListener { _, _, position, _ ->
            showEditDeleteDialog(transactionList[position])
            true
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        transactionList = dbHelper.getTransactionsByType(type)
        val adapter = object : ArrayAdapter<Transaction>(this, android.R.layout.simple_list_item_2, android.R.id.text1, transactionList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val text1 = view.findViewById<TextView>(android.R.id.text1)
                val text2 = view.findViewById<TextView>(android.R.id.text2)

                val transaction = transactionList[position]
                val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

                text1.text = "${transaction.deskripsi} (${transaction.bankAkun}) - ${formatRupiah.format(transaction.jumlah)}"
                text2.text = transaction.tanggal

                return view
            }
        }
        lvHistory.adapter = adapter
    }

    private fun showEditDeleteDialog(transaction: Transaction) {
        val options = arrayOf("Edit", "Hapus")

        AlertDialog.Builder(this)
            .setTitle("Pilih Aksi")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, AddTransactionActivity::class.java).apply {
                            putExtra("IS_EDIT_MODE", true)
                            putExtra("TRANSACTION_ID", transaction.id)
                        }
                        startActivity(intent)
                    }
                    1 -> {
                        showDeleteConfirmationDialog(transaction)
                    }
                }
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi ini? Aksi ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                val success = dbHelper.deleteTransaction(transaction.id)
                if (success) {
                    Toast.makeText(this, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show()
                    loadHistory()
                } else {
                    Toast.makeText(this, "Gagal menghapus transaksi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}