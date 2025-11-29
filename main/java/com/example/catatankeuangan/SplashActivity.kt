package com.example.catatankeuangan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Sembunyikan ActionBar untuk tampilan layar penuh
        supportActionBar?.hide()

        // Atur waktu tunda sebelum pindah ke MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Tutup SplashActivity agar tidak bisa kembali
        }, 3000) // 3000 milidetik = 3 detik
    }
}