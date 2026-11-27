package com.txttitle.sakuku.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.txttitle.sakuku.R
import com.txttitle.sakuku.adapter.TransaksiAdapter
import com.txttitle.sakuku.data.RingkasanData
import com.txttitle.sakuku.data.TransaksiData
import com.txttitle.sakuku.databinding.FragmentBerandaBinding
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class BerandaFragment : Fragment() {

    private lateinit var binding: FragmentBerandaBinding
    var recentList: ArrayList<TransaksiData> = ArrayList()
    private var isDataTerhubung = false
    private var requestQueue: RequestQueue? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Teks "Lihat Semua" saat diklik pindah ke tab Riwayat
        binding.txtLihatSemua.setOnClickListener {
            activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.itemRiwayat
        }

        // Klik ikon status untuk langsung cek ulang koneksi server tersambung / terputus
        binding.imgStatusData.setOnClickListener {
            binding.imgStatusData.alpha = 0.45f
            requestQueue?.cancelAll("BERANDA")
            loadRingkasan(tampilkanPesan = true)
            loadRecentTransaksi()
        }
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
        binding.imgStatusData.alpha = 0.6f
        requestQueue?.cancelAll("BERANDA")
        loadRingkasan(tampilkanPesan = false)
        loadRecentTransaksi()
    }

    override fun onDestroyView() {
        requestQueue?.cancelAll("BERANDA")
        super.onDestroyView()
    }

    private fun getQueue(): RequestQueue? {
        val appCtx = context?.applicationContext ?: return null
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(appCtx)
        }
        return requestQueue
    }

    // Format angka nominal dengan pemisah ribuan titik
    private fun formatAngka(nilai: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(nilai)
    }

    // untuk perbarui warna & ikon status koneksi server
    private fun updateStatusData(terhubung: Boolean, tampilkanPesan: Boolean = false) {
        if (!isAdded || view == null) return
        isDataTerhubung = terhubung
        binding.imgStatusData.alpha = 1.0f
        context?.let { ctx ->
            if (terhubung) {
                binding.imgStatusData.setImageResource(R.drawable.baseline_cloud_done_24)
                binding.imgStatusData.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.pine_primary)
                )
            } else {
                binding.imgStatusData.setImageResource(R.drawable.baseline_cloud_off_24)
                binding.imgStatusData.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, R.color.expense_rose)
                )
            }
            if (tampilkanPesan) {
                val pesan = if (terhubung) {
                    "Status Data: Tersambung ke server"
                } else {
                    "Status Data: Terputus dari server"
                }
                Toast.makeText(ctx, pesan, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Menampilkan ucapan dengan nama pengguna yang tersimpan
    private fun updateGreeting() {
        val ctx = context ?: return
        val shared = ctx.getSharedPreferences("SakuKuPreferences", Context.MODE_PRIVATE)
        val savedName = shared.getString("user_display_name", "") ?: ""
        if (savedName.isNotEmpty()) {
            binding.txtGreeting.text = "Halo, $savedName"
        } else {
            binding.txtGreeting.text = "Halo, Pengguna"
        }
    }

    // Ambil ringkasan saldo, pemasukan, pengeluaran dari get_ringkasan.php
    private fun loadRingkasan(tampilkanPesan: Boolean) {
        val q = getQueue() ?: return
        val url = "http://10.0.2.2/sakuku/get_ringkasan.php"
        val sr = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                if (isAdded && view != null) {
                    try {
                        val obj = JSONObject(response)
                        if (obj.getString("result") == "SUCCESS") {
                            updateStatusData(true, tampilkanPesan)
                            val data = obj.getJSONObject("data")
                            val ringkasan = Gson().fromJson(data.toString(), RingkasanData::class.java)

                            binding.txtTotalSaldo.text = if (ringkasan.total_saldo < 0) {
                                "-Rp ${formatAngka(-ringkasan.total_saldo)}"
                            } else {
                                "Rp ${formatAngka(ringkasan.total_saldo)}"
                            }
                            binding.txtTotalMasuk.text = "+Rp ${formatAngka(ringkasan.total_masuk)}"
                            binding.txtTotalKeluar.text = "-Rp ${formatAngka(ringkasan.total_keluar)}"

                            // Hitung persentase pemakaian pemasukan menggunakan double agar nilai < 1% terlihat
                            val persen = if (ringkasan.total_masuk > 0) {
                                (ringkasan.total_keluar / ringkasan.total_masuk) * 100.0
                            } else if (ringkasan.total_keluar > 0) {
                                100.0
                            } else {
                                0.0
                            }

                            val progressSkala = (persen.coerceIn(0.0, 100.0) * 100).toInt()
                            binding.progressAnggaran.max = 10000
                            binding.progressAnggaran.progress = progressSkala

                            val formatPersen = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
                            formatPersen.minimumFractionDigits = 0
                            formatPersen.maximumFractionDigits = 2
                            binding.txtPersenAnggaran.text = "${formatPersen.format(persen)}% terpakai"

                            if (ringkasan.total_keluar <= 0.0) {
                                binding.txtKetAnggaran.text = "Belum ada pengeluaran tercatat."
                            } else if (persen > 80.0) {
                                binding.txtKetAnggaran.text = "Pengeluaran sudah melebihi 80% pemasukan."
                            } else {
                                binding.txtKetAnggaran.text = "Pengeluaran masih di bawah pemasukan."
                            }
                        } else {
                            updateStatusData(false, tampilkanPesan)
                        }
                    } catch (e: Exception) {
                        updateStatusData(false, tampilkanPesan)
                    }
                }
            },
            {
                it.printStackTrace()
                if (isAdded && view != null) {
                    updateStatusData(false, tampilkanPesan)
                }
            }
        )
        sr.setShouldCache(false)
        sr.retryPolicy = DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        sr.tag = "BERANDA"
        q.add(sr)
    }

    // Ambil maksimal 5 transaksi terbaru dari get_transaksi.php
    private fun loadRecentTransaksi() {
        val q = getQueue() ?: return
        val url = "http://10.0.2.2/sakuku/get_transaksi.php"
        val sr = StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if (isAdded && view != null) {
                    try {
                        val obj = JSONObject(response)
                        if (obj.getString("result") == "SUCCESS") {
                            val data = obj.getJSONArray("data")
                            val sType = object : TypeToken<List<TransaksiData>>() {}.type
                            val allList = Gson().fromJson<ArrayList<TransaksiData>>(data.toString(), sType)

                            recentList.clear()
                            val batas = if (allList.size > 5) 5 else allList.size
                            for (i in 0 until batas) {
                                recentList.add(allList[i])
                            }

                            with(binding.rvRecentTransaksi) {
                                layoutManager = LinearLayoutManager(context)
                                setHasFixedSize(true)
                                adapter = TransaksiAdapter(recentList)
                            }
                        } else {
                            updateStatusData(false)
                        }
                    } catch (e: Exception) {
                        updateStatusData(false)
                    }
                }
            },
            {
                it.printStackTrace()
                if (isAdded && view != null) {
                    updateStatusData(false)
                }
            }
        )
        sr.setShouldCache(false)
        sr.retryPolicy = DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        sr.tag = "BERANDA"
        q.add(sr)
    }
}
