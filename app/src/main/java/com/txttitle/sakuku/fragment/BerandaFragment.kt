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
import com.android.volley.Request
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Teks "Lihat Semua" pindah ke tab Riwayat
        binding.txtLihatSemua.setOnClickListener {
            activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.itemRiwayat
        }

        // Klik ikon status untuk melihat keterangan & menyegarkan data server
        binding.imgStatusData.setOnClickListener {
            val pesan = if (isDataTerhubung) {
                "Status Data: Tersambung ke server"
            } else {
                "Status Data: Terputus dari server"
            }
            Toast.makeText(activity, pesan, Toast.LENGTH_SHORT).show()
            loadRingkasan()
            loadRecentTransaksi()
        }
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
        loadRingkasan()
        loadRecentTransaksi()
    }

    // Format angka nominal dengan pemisah ribuan titik (contoh: 16.476.000)
    private fun formatAngka(nilai: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(nilai)
    }

    // Perbarui warna & ikon status koneksi server (pine_primary = terhubung, expense_rose = terputus)
    private fun updateStatusData(terhubung: Boolean) {
        isDataTerhubung = terhubung
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
        }
    }

    // Menampilkan ucapan dengan nama pengguna yang tersimpan
    private fun updateGreeting() {
        val shared = requireActivity().getSharedPreferences("SakuKuPreferences", Context.MODE_PRIVATE)
        val savedName = shared.getString("user_display_name", "") ?: ""
        if (savedName.isNotEmpty()) {
            binding.txtGreeting.text = "Halo, $savedName"
        } else {
            binding.txtGreeting.text = "Halo, Pengguna"
        }
    }

    // Ambil ringkasan saldo, pemasukan, pengeluaran dari get_ringkasan.php
    private fun loadRingkasan() {
        val url = "http://10.0.2.2/sakuku/get_ringkasan.php"
        val q = Volley.newRequestQueue(activity)
        val sr = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                if (activity != null) {
                    try {
                        val obj = JSONObject(response)
                        if (obj.getString("result") == "SUCCESS") {
                            updateStatusData(true)
                            val data = obj.getJSONObject("data")
                            val ringkasan = Gson().fromJson(data.toString(), RingkasanData::class.java)

                            binding.txtTotalSaldo.text = if (ringkasan.total_saldo < 0) {
                                "-Rp ${formatAngka(-ringkasan.total_saldo)}"
                            } else {
                                "Rp ${formatAngka(ringkasan.total_saldo)}"
                            }
                            binding.txtTotalMasuk.text = "+Rp ${formatAngka(ringkasan.total_masuk)}"
                            binding.txtTotalKeluar.text = "-Rp ${formatAngka(ringkasan.total_keluar)}"

                            // Hitung persentase pemakaian anggaran untuk ProgressBar
                            val persen = if (ringkasan.total_masuk > 0) {
                                ((ringkasan.total_keluar / ringkasan.total_masuk) * 100).toInt()
                            } else if (ringkasan.total_keluar > 0) {
                                100
                            } else {
                                0
                            }

                            val progressNilai = if (persen > 100) 100 else persen
                            binding.progressAnggaran.max = 100
                            binding.progressAnggaran.progress = progressNilai
                            binding.txtPersenAnggaran.text = "$persen% terpakai"

                            if (persen > 80) {
                                binding.txtKetAnggaran.text = "Pengeluaran mendekati atau melebihi pemasukan bulan ini."
                            } else {
                                binding.txtKetAnggaran.text = "Sisa saldo terkontrol aman untuk bulan berjalan."
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
                if (activity != null) {
                    updateStatusData(false)
                }
            }
        )
        q.add(sr)
    }

    // Ambil maksimal 5 transaksi terbaru dari get_transaksi.php
    private fun loadRecentTransaksi() {
        val url = "http://10.0.2.2/sakuku/get_transaksi.php"
        val q = Volley.newRequestQueue(activity)
        val sr = StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if (activity != null) {
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
                                layoutManager = LinearLayoutManager(activity)
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
                if (activity != null) {
                    updateStatusData(false)
                }
            }
        )
        q.add(sr)
    }
}
