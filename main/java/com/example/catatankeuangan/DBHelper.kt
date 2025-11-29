package com.example.catatankeuangan

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "KeuanganDB.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_GOALS = "goals"
        private const val TABLE_TRANSAKSI = "transaksi"
        private const val GOAL_ID = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_GOALS_TABLE = """
            CREATE TABLE $TABLE_GOALS (
                id INTEGER PRIMARY KEY, 
                nama_goals TEXT, 
                target_jumlah REAL, 
                terkumpul REAL DEFAULT 0.0,
                deadline TEXT
            )
        """.trimIndent()
        db.execSQL(CREATE_GOALS_TABLE)

        val CREATE_TRANSAKSI_TABLE = """
            CREATE TABLE $TABLE_TRANSAKSI (
                id INTEGER PRIMARY KEY AUTOINCREMENT, 
                jenis TEXT, 
                jumlah REAL, 
                bank_akun TEXT, 
                deskripsi TEXT, 
                tanggal TEXT
            )
        """.trimIndent()
        db.execSQL(CREATE_TRANSAKSI_TABLE)

        val goalValues = ContentValues().apply {
            put("id", GOAL_ID)
            put("nama_goals", "Dana Darurat")
            put("target_jumlah", 10000000.00)
            put("terkumpul", 0.00)
            put("deadline", "")
        }
        db.insert(TABLE_GOALS, null, goalValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_GOALS ADD COLUMN deadline TEXT")
        }
    }

    private fun adjustGoalBalance(amount: Double, isPemasukan: Boolean) {
        val db = this.writableDatabase
        val cursor = db.rawQuery("SELECT terkumpul FROM $TABLE_GOALS WHERE id = $GOAL_ID", null)
        if (cursor.moveToFirst()) {
            val currentTerkumpul = cursor.getDouble(0)
            val newTerkumpul = if (isPemasukan) currentTerkumpul + amount else currentTerkumpul - amount
            val goalUpdateValues = ContentValues().apply {
                put("terkumpul", newTerkumpul)
            }
            db.update(TABLE_GOALS, goalUpdateValues, "id = $GOAL_ID", null)
        }
        cursor.close()
    }

    fun addTransaction(transaction: Transaction): Boolean {
        val db = this.writableDatabase
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val tanggalSaatIni = dateFormat.format(Date())

        val transValues = ContentValues().apply {
            put("jenis", transaction.jenis)
            put("jumlah", transaction.jumlah)
            put("bank_akun", transaction.bankAkun)
            put("deskripsi", transaction.deskripsi)
            put("tanggal", tanggalSaatIni)
        }
        val success = db.insert(TABLE_TRANSAKSI, null, transValues) > 0
        if (success) {
            adjustGoalBalance(transaction.jumlah, transaction.jenis == "Pemasukan")
        }
        db.close()
        return success
    }

    fun updateTransaction(transaction: Transaction): Boolean {
        val db = this.writableDatabase
        val oldTransaction = getTransactionById(transaction.id)

        if (oldTransaction != null) {
            val amountDifference = transaction.jumlah - oldTransaction.jumlah
            // This logic assumes the 'jenis' (type) of transaction cannot be changed.
            // If it could, the logic would be more complex.
            adjustGoalBalance(amountDifference, transaction.jenis == "Pemasukan")
        }

        val values = ContentValues().apply {
            put("jenis", transaction.jenis)
            put("jumlah", transaction.jumlah)
            put("bank_akun", transaction.bankAkun)
            put("deskripsi", transaction.deskripsi)
        }

        val success = db.update(TABLE_TRANSAKSI, values, "id = ${transaction.id}", null) > 0
        db.close()
        return success
    }

    fun deleteTransaction(transactionId: Int): Boolean {
        val db = this.writableDatabase
        val transaction = getTransactionById(transactionId)
        if (transaction != null) {
            // Reverse the amount to subtract it from the balance
            adjustGoalBalance(-transaction.jumlah, transaction.jenis == "Pemasukan")
        }
        val success = db.delete(TABLE_TRANSAKSI, "id = $transactionId", null) > 0
        db.close()
        return success
    }
    
    fun getTransactionById(transactionId: Int): Transaction? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSAKSI WHERE id = ?", arrayOf(transactionId.toString()))
        var transaction: Transaction? = null
        if (cursor.moveToFirst()) {
            transaction = Transaction(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                jenis = cursor.getString(cursor.getColumnIndexOrThrow("jenis")),
                jumlah = cursor.getDouble(cursor.getColumnIndexOrThrow("jumlah")),
                bankAkun = cursor.getString(cursor.getColumnIndexOrThrow("bank_akun")),
                deskripsi = cursor.getString(cursor.getColumnIndexOrThrow("deskripsi")),
                tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"))
            )
        }
        cursor.close()
        return transaction
    }

    fun getGoals(): Map<String, Any>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT nama_goals, target_jumlah, terkumpul, deadline FROM $TABLE_GOALS WHERE id = $GOAL_ID", null)
        var result: Map<String, Any>? = null
        if (cursor.moveToFirst()) {
            result = mapOf(
                "nama" to cursor.getString(cursor.getColumnIndexOrThrow("nama_goals")),
                "target" to cursor.getDouble(cursor.getColumnIndexOrThrow("target_jumlah")),
                "terkumpul" to cursor.getDouble(cursor.getColumnIndexOrThrow("terkumpul")),
                "deadline" to (cursor.getString(cursor.getColumnIndexOrThrow("deadline")) ?: "")
            )
        }
        cursor.close()
        return result
    }

    fun updateGoal(goalName: String, targetAmount: Double, deadline: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("nama_goals", goalName)
            put("target_jumlah", targetAmount)
            put("deadline", deadline)
        }
        return db.update(TABLE_GOALS, values, "id = $GOAL_ID", null) > 0
    }

    fun getTransactionsByType(type: String): List<Transaction> {
        val transactionList = mutableListOf<Transaction>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSAKSI WHERE jenis = ? ORDER BY tanggal DESC", arrayOf(type))
        if (cursor.moveToFirst()) {
            do {
                transactionList.add(Transaction(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    jenis = cursor.getString(cursor.getColumnIndexOrThrow("jenis")),
                    jumlah = cursor.getDouble(cursor.getColumnIndexOrThrow("jumlah")),
                    bankAkun = cursor.getString(cursor.getColumnIndexOrThrow("bank_akun")),
                    deskripsi = cursor.getString(cursor.getColumnIndexOrThrow("deskripsi")),
                    tanggal = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return transactionList
    }
}