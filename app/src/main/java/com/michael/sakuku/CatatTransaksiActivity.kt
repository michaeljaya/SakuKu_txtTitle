package com.michael.sakuku

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.michael.sakuku.databinding.ActivityCatatTransaksiBinding

class CatatTransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatatTransaksiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatatTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tombol Kembali
        binding.btnBackCatat.setOnClickListener {
            finish()
        }

        // Setup pilihan kategori
        val kategoriList = arrayOf(
            "Makan & Minum", "Transportasi", "Tagihan",
            "Belanja", "Kesehatan", "Gaji", "Bonus", "Lainnya"
        )
        val katAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        katAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategori.adapter = katAdapter

        // Setup Pilihan Sumber Kantong
        val kantongList = arrayOf("Tunai", "Bank BCA", "GoPay", "OVO", "ShopeePay")
        val kantongAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kantongList)
        kantongAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKantong.adapter = kantongAdapter
    }
}
