package com.example.catatankeuangan

import java.io.Serializable

data class Transaction(
    val id: Int,
    val jenis: String,
    val jumlah: Double,
    val bankAkun: String,
    val deskripsi: String,
    val tanggal: String
) : Serializable
