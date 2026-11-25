package com.txttitle.sakuku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.txttitle.sakuku.data.TransaksiData
import com.txttitle.sakuku.databinding.ActivityDetailTransaksiBinding
import java.text.NumberFormat
import java.util.Locale

class DetailTransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTransaksiBinding
    private var transaksi: TransaksiData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tombol Kembali
        binding.btnBackDetail.setOnClickListener {
            finish()
        }

        // Ambil data transaksi dari intent
        transaksi = intent.getSerializableExtra("DATA_TRANSAKSI") as? TransaksiData

        if (transaksi != null) {
            val item = transaksi!!
            val formatRp = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            binding.txtDetailJudul.text = item.judul
            binding.txtDetailNominal.text = formatRp.format(item.nominal)
            binding.txtDetailKategori.text = "Kategori: " + item.kategori
            binding.txtDetailKantong.text = "Sumber Kantong: " + item.kantong
            binding.txtDetailTanggal.text = "Waktu: " + item.tanggal
            binding.txtDetailCatatan.text = "Catatan: " + if (!item.catatan.isNullOrEmpty()) item.catatan else "-"
            binding.txtDetailPrioritas.text = "Status: " + if (item.is_prioritas == 1) "Pokok / Prioritas" else "Biasa"
        }
    }
}
