package com.txttitle.sakuku

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.txttitle.sakuku.data.TransaksiData
import com.txttitle.sakuku.databinding.ActivityDetailTransaksiBinding
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class DetailTransaksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTransaksiBinding
    private var transaksi: TransaksiData? = null
    private var requestQueue: RequestQueue? = null

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
            val nominalFormat = formatAngka(item.nominal)

            binding.txtDetailJudul.text = item.judul
            if (item.tipe == "PEMASUKAN") {
                binding.txtDetailNominal.text = "+Rp $nominalFormat"
                binding.txtDetailNominal.setTextColor(getColor(R.color.income_green))
            } else {
                binding.txtDetailNominal.text = "-Rp $nominalFormat"
                binding.txtDetailNominal.setTextColor(getColor(R.color.expense_rose))
            }

            binding.txtDetailKategori.text = "Kategori: " + item.kategori
            binding.txtDetailKantong.text = "Sumber Kantong: " + item.kantong
            binding.txtDetailTanggal.text = "Waktu: " + item.tanggal
            binding.txtDetailCatatan.text = "Catatan: " + if (!item.catatan.isNullOrEmpty()) item.catatan else "-"
            binding.txtDetailPrioritas.text = "Status: " + if (item.is_prioritas == 1) "Pokok / Prioritas" else "Biasa"
        }

        // Tombol Bagikan Rincian menggunakan implicit intent
        binding.btnBagikan.setOnClickListener {
            if (transaksi != null) {
                val item = transaksi!!
                val nominalFormat = formatAngka(item.nominal)
                val tanda = if (item.tipe == "PEMASUKAN") "+Rp" else "-Rp"
                val catatanTeks = if (!item.catatan.isNullOrEmpty()) item.catatan else "-"

                val pesan = "Catatan Keuangan SakuKu\n" +
                        "Judul: ${item.judul}\n" +
                        "Nominal: $tanda $nominalFormat (${item.tipe})\n" +
                        "Kategori: ${item.kategori}\n" +
                        "Kantong: ${item.kantong}\n" +
                        "Waktu: ${item.tanggal}\n" +
                        "Catatan: $catatanTeks"

                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT, pesan)
                intent.type = "text/plain"

                val shareIntent = Intent.createChooser(intent, "Bagikan via")
                startActivity(shareIntent)
            }
        }

        // Tombol Hapus Catatan
        binding.btnHapus.setOnClickListener {
            if (transaksi != null) {
                hapusTransaksi(transaksi!!.id)
            }
        }
    }

    override fun onDestroy() {
        requestQueue?.cancelAll("DETAIL")
        super.onDestroy()
    }

    private fun formatAngka(nilai: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(nilai)
    }

    // Kirim request hapus ke hapus_transaksi.php via Volley POST
    private fun hapusTransaksi(id: Int) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(applicationContext)
        }
        binding.btnHapus.isEnabled = false
        val url = "http://10.0.2.2/sakuku/hapus_transaksi.php"
        val sr = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if (!isFinishing && !isDestroyed) {
                    binding.btnHapus.isEnabled = true
                    try {
                        val obj = JSONObject(response)
                        if (obj.getString("result") == "SUCCESS") {
                            Toast.makeText(this, "Transaksi berhasil dihapus!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, obj.optString("message", "Gagal menghapus catatan!"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Respons server tidak valid!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            {
                it.printStackTrace()
                if (!isFinishing && !isDestroyed) {
                    binding.btnHapus.isEnabled = true
                    Toast.makeText(this, "Gagal terhubung ke server!", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            override fun getParams() = hashMapOf(
                "id" to id.toString()
            )
        }
        sr.setShouldCache(false)
        sr.retryPolicy = DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        sr.tag = "DETAIL"
        requestQueue?.add(sr)
    }
}
