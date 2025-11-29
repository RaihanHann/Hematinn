package com.example.catatankeuangan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    private lateinit var rgJenis: RadioGroup
    private lateinit var etJumlah: EditText
    private lateinit var etBankAkun: EditText
    private lateinit var etDeskripsi: EditText
    private lateinit var btnSimpan: Button
    private lateinit var tvTitle: TextView

    private var isEditMode = false
    private var transactionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        dbHelper = DBHelper(this)

        rgJenis = findViewById(R.id.rg_jenis)
        etJumlah = findViewById(R.id.et_jumlah)
        etBankAkun = findViewById(R.id.et_bank_akun)
        etDeskripsi = findViewById(R.id.et_deskripsi)
        btnSimpan = findViewById(R.id.btn_simpan_transaksi)
        tvTitle = findViewById(R.id.tv_add_transaction_title)

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        transactionId = intent.getIntExtra("TRANSACTION_ID", -1)

        if (isEditMode) {
            tvTitle.text = "✏️ Edit Transaksi"
            btnSimpan.text = "SIMPAN PERUBAHAN"
            loadTransactionDetails()
        } else {
            tvTitle.text = "📝 Tambah Transaksi"
            btnSimpan.text = "SIMPAN TRANSAKSI"
        }

        btnSimpan.setOnClickListener {
            saveTransaction()
        }
    }

    private fun loadTransactionDetails() {
        val transaction = dbHelper.getTransactionById(transactionId)
        if (transaction != null) {
            etJumlah.setText(transaction.jumlah.toString())
            etBankAkun.setText(transaction.bankAkun)
            etDeskripsi.setText(transaction.deskripsi)
            if (transaction.jenis == "Pemasukan") {
                rgJenis.check(R.id.rb_pemasukan)
            } else {
                rgJenis.check(R.id.rb_pengeluaran)
            }
            // Disable changing the transaction type (Pemasukan/Pengeluaran)
            // as it complicates the balance adjustment logic.
            for (i in 0 until rgJenis.childCount) {
                rgJenis.getChildAt(i).isEnabled = false
            }
        }
    }

    private fun saveTransaction() {
        val selectedId = rgJenis.checkedRadioButtonId
        val jenis = if (selectedId == R.id.rb_pemasukan) "Pemasukan" else "Pengeluaran"
        
        val jumlahText = etJumlah.text.toString()
        val bankAkun = etBankAkun.text.toString().trim()
        val deskripsi = etDeskripsi.text.toString().trim()

        if (jumlahText.isEmpty() || bankAkun.isEmpty() || deskripsi.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlah = jumlahText.toDoubleOrNull()
        if (jumlah == null || jumlah <= 0) {
            Toast.makeText(this, "Jumlah harus berupa angka positif.", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(transactionId, jenis, jumlah, bankAkun, deskripsi, "") // Tanggal is handled by DBHelper

        val success = if (isEditMode) {
            dbHelper.updateTransaction(transaction)
        } else {
            dbHelper.addTransaction(transaction)
        }

        if (success) {
            val message = if (isEditMode) "Transaksi berhasil diperbarui" else "Transaksi berhasil disimpan"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            finish()
        } else {
            val message = if (isEditMode) "Gagal memperbarui transaksi" else "Gagal menyimpan transaksi"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}