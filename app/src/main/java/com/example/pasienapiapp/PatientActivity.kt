package com.example.pasienapiapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pasienapiapp.adapter.PasienAdapter
import com.example.pasienapiapp.network.RetrofitClient
import kotlinx.coroutines.launch

class PatientActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var rvPasien: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: PasienAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient)

        tvUserName  = findViewById(R.id.tvUserName)
        progressBar = findViewById(R.id.progressBarPasien)
        rvPasien    = findViewById(R.id.rvPasien)
        tvEmpty     = findViewById(R.id.tvEmpty)

        adapter = PasienAdapter()
        rvPasien.layoutManager = LinearLayoutManager(this)
        rvPasien.adapter = adapter

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val name  = prefs.getString("name", "") ?: ""

        tvUserName.text = name

        if (token.isEmpty()) {
            showMessage("Sesi habis, silakan login ulang")
            finish(); return
        }

        loadPasien(token)
    }

    private fun loadPasien(token: String) {
        lifecycleScope.launch {
            showLoading(true)
            try {
                val response = RetrofitClient.apiService.getPasien("Bearer $token")
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    adapter.setData(list)
                    tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    showMessage("Gagal mengambil data: ${response.code()}")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
