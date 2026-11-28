package com.txttitle.sakuku.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.txttitle.sakuku.DetailTransaksiActivity
import com.txttitle.sakuku.R
import com.txttitle.sakuku.data.RingkasanData
import com.txttitle.sakuku.data.TransaksiData
import com.txttitle.sakuku.databinding.FragmentAnalisisBinding
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

class AnalisisFragment : Fragment() {

    private lateinit var binding: FragmentAnalisisBinding
    private var requestQueue: RequestQueue? = null
    private var permintaanAktif = 0
    private var adaGagalMuat = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalisisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCobaLagiAnalisis.setOnClickListener {
            muatSemuaAnalisis()
        }
    }

    override fun onResume() {
        super.onResume()
        muatSemuaAnalisis()
    }

    override fun onDestroyView() {
        requestQueue?.cancelAll("ANALISIS")
        super.onDestroyView()
    }

    // Jalankan 2 request paralel agar status awan dan card error menunggu keduanya selesai
    private fun muatSemuaAnalisis() {
        requestQueue?.cancelAll("ANALISIS")
        permintaanAktif = 2
        adaGagalMuat = false
        binding.cardErrorAnalisis.visibility = View.GONE
        binding.imgStatusAnalisis.alpha = 0.45f
        loadRingkasanAnalisis()
        loadDetailTransaksiAnalisis()
    }

    private fun tandaiPermintaanSelesai(berhasil: Boolean) {
        if (!isAdded || view == null) return
        if (!berhasil) {
            adaGagalMuat = true
            binding.cardErrorAnalisis.visibility = View.VISIBLE
        }
        permintaanAktif = (permintaanAktif - 1).coerceAtLeast(0)
        if (permintaanAktif == 0) {
            perbaruiIkonStatus(!adaGagalMuat)
        }
    }

    private fun perbaruiIkonStatus(terhubung: Boolean) {
        val ctx = context ?: return
        binding.imgStatusAnalisis.alpha = 1.0f
        if (terhubung) {
            binding.imgStatusAnalisis.setImageResource(R.drawable.baseline_cloud_done_24)
            binding.imgStatusAnalisis.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, R.color.pine_primary)
            )
        } else {
            binding.imgStatusAnalisis.setImageResource(R.drawable.baseline_cloud_off_24)
            binding.imgStatusAnalisis.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, R.color.expense_rose)
            )
        }
    }

    private fun getQueue(): RequestQueue? {
        val appCtx = context?.applicationContext ?: return null
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(appCtx)
        }
        return requestQueue
    }

    private fun formatAngka(nilai: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(nilai)
    }

    // pembulatan (bagian / total) * 100. Contoh: (34.000 / 164.000) * 100 = 20,73% -> 21%
    private fun hitungPersen(bagian: Double, total: Double): Int {
        if (total <= 0.0) return 0
        return ((bagian / total) * 100.0).roundToInt().coerceIn(0, 100)
    }

    private fun loadRingkasanAnalisis() {
        val q = getQueue() ?: return
        val url = "http://10.0.2.2/sakuku/get_ringkasan.php"
        val sr = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                if (isAdded && view != null) {
                    var berhasil = false
                    try {
                        val obj = JSONObject(response)
                        if (obj.getString("result") == "SUCCESS") {
                            val data = obj.getJSONObject("data")
                            val ringkasan = Gson().fromJson(data.toString(), RingkasanData::class.java)
                            tampilkanRingkasan(ringkasan)
                            berhasil = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    tandaiPermintaanSelesai(berhasil)
                }
            },
            { error ->
                error.printStackTrace()
                tandaiPermintaanSelesai(false)
            }
        )
        sr.setShouldCache(false)
        sr.retryPolicy = DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        sr.tag = "ANALISIS"
        q.add(sr)
    }

    private fun tampilkanRingkasan(ringkasan: RingkasanData) {
        val ctx = context ?: return
        binding.txtJumlahTransaksiAnalisis.text = "${ringkasan.total_transaksi} transaksi"
        binding.txtAnalisisMasuk.text = "+Rp ${formatAngka(ringkasan.total_masuk)}"
        binding.txtAnalisisKeluar.text = "-Rp ${formatAngka(ringkasan.total_keluar)}"

        // MENGHITUNG ARUS KAS BERSIH: total_masuk - total_keluar.
        // Contoh: 10.000.000 - 150.000 = +9.850.000
        val selisih = ringkasan.total_masuk - ringkasan.total_keluar
        if (selisih >= 0) {
            binding.txtSelisihAnalisis.text = "+Rp ${formatAngka(selisih)}"
            binding.txtSelisihAnalisis.setTextColor(ContextCompat.getColor(ctx, R.color.income_green))
        } else {
            binding.txtSelisihAnalisis.text = "-Rp ${formatAngka(-selisih)}"
            binding.txtSelisihAnalisis.setTextColor(ContextCompat.getColor(ctx, R.color.expense_rose))
        }
    }

    // Mengambil semua tipe sekali request untuk mengisi kategori keluar, prioritas, kantong, top 3, dan sumber masuk
    private fun loadDetailTransaksiAnalisis() {
        val q = getQueue() ?: return
        val url = "http://10.0.2.2/sakuku/get_transaksi.php"
        val sr = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if (isAdded && view != null) {
                    var berhasil = false
                    try {
                        val obj = JSONObject(response)
                        if (obj.getString("result") == "SUCCESS") {
                            val data = obj.getJSONArray("data")
                            val sType = object : TypeToken<List<TransaksiData>>() {}.type
                            val semuaTransaksi = Gson().fromJson<ArrayList<TransaksiData>>(data.toString(), sType)
                            renderAnalisisTransaksi(semuaTransaksi)
                            berhasil = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    tandaiPermintaanSelesai(berhasil)
                }
            },
            { error ->
                error.printStackTrace()
                tandaiPermintaanSelesai(false)
            }
        ) {
            override fun getParams() = hashMapOf("tipe" to "SEMUA")
        }
        sr.setShouldCache(false)
        sr.retryPolicy = DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        sr.tag = "ANALISIS"
        q.add(sr)
    }

    private fun renderAnalisisTransaksi(semua: List<TransaksiData>) {
        val listKeluar = semua.filter { it.tipe == "PENGELUARAN" }
        val listMasuk = semua.filter { it.tipe == "PEMASUKAN" }
        val totalKeluar = listKeluar.sumOf { it.nominal }
        val totalMasuk = listMasuk.sumOf { it.nominal }

        // MENGHITUNH RATA2: totalKeluar / jumlah transaksi keluar.
        // Contoh: 150.000 / 4 transaksi = 37.500
        val rataKeluar = if (listKeluar.isNotEmpty()) totalKeluar / listKeluar.size else 0.0
        binding.txtRataKeluarAnalisis.text = "Rp ${formatAngka(rataKeluar)} / transaksi"

        tampilkanKategoriKeluar(listKeluar, totalKeluar)
        tampilkanPrioritas(listKeluar, totalKeluar)
        tampilkanKantong(listKeluar, totalKeluar)
        tampilkanTopPengeluaran(listKeluar)
        tampilkanSumberMasuk(listMasuk, totalMasuk)
    }

    private fun tampilkanKategoriKeluar(listKeluar: List<TransaksiData>, totalKeluar: Double) {
        val kosong = listKeluar.isEmpty()
        binding.txtKosongKategori.visibility = if (kosong) View.VISIBLE else View.GONE
        binding.containerKategori.visibility = if (kosong) View.GONE else View.VISIBLE
        if (kosong) return

        val perKategori = mutableMapOf(
            "Makan & Minum" to 0.0,
            "Tagihan" to 0.0,
            "Transportasi" to 0.0,
            "Belanja" to 0.0,
            "Kesehatan" to 0.0,
            "Lainnya" to 0.0
        )
        // Kategori di luar 5 nama utama masuk ke Lainnya agar lengkap
        for (item in listKeluar) {
            val kunci = if (perKategori.containsKey(item.kategori)) item.kategori else "Lainnya"
            perKategori[kunci] = perKategori.getValue(kunci) + item.nominal
        }

        // PERHITUNGAN PENGELUARAN PER KATEGORI: (Kategori / Total Keluar) * 100
        // Total Keluar = 550.000
        // Misal Kategori makan & minum, total kategorinya 350.000
        // dan kategori total belanja 200.000
        // (350.000 / 550.000) * 100 = 63,63 => 64% ini makan & minum
        // (200.000 / 550.000) * 100 = 36,36 => 36% ini belanja, *yg jika ditotal maka 100 karena kategori lain belum ada*

        bindKategoriRow("Makan & Minum", perKategori.getValue("Makan & Minum"), totalKeluar,
            binding.lblKatMakan, binding.txtNomMakan, binding.progMakan)
        bindKategoriRow("Tagihan", perKategori.getValue("Tagihan"), totalKeluar,
            binding.lblKatTagihan, binding.txtNomTagihan, binding.progTagihan)
        bindKategoriRow("Transportasi", perKategori.getValue("Transportasi"), totalKeluar,
            binding.lblKatTransport, binding.txtNomTransport, binding.progTransport)
        bindKategoriRow("Belanja", perKategori.getValue("Belanja"), totalKeluar,
            binding.lblKatBelanja, binding.txtNomBelanja, binding.progBelanja)
        bindKategoriRow("Kesehatan", perKategori.getValue("Kesehatan"), totalKeluar,
            binding.lblKatKesehatan, binding.txtNomKesehatan, binding.progKesehatan)
        bindKategoriRow("Lainnya", perKategori.getValue("Lainnya"), totalKeluar,
            binding.lblKatLainnya, binding.txtNomLainnya, binding.progLainnya)
    }

    private fun bindKategoriRow(
        nama: String,
        nilai: Double,
        total: Double,
        labelView: TextView,
        nominalView: TextView,
        progressBar: LinearProgressIndicator
    ) {
        val persen = hitungPersen(nilai, total)
        labelView.text = "$nama ($persen%)"
        nominalView.text = "Rp ${formatAngka(nilai)}"
        progressBar.max = 100
        progressBar.progress = persen
    }

    private fun tampilkanPrioritas(listKeluar: List<TransaksiData>, totalKeluar: Double) {
        val totalPokok = listKeluar.filter { it.is_prioritas == 1 }.sumOf { it.nominal }
        val totalBiasa = (totalKeluar - totalPokok).coerceAtLeast(0.0)
        // MENGHITUNG PRIORITAS VS BIASA:
        // Contoh:
        // Jumlah semua pengeluaran (transaksi pokok): 350.000, yg (biasa) 200k
        // Misal pengeluaran 550.000 => didapat dari pokok 350k + biasa 200k
        // PERHITUNGAN POKOK: (transaksi pokok / pengeluaran) * 100 => (350.000 / 550.000) * 100 = 63.6% ya 64 lh
        // PERHITUNGAN BIASA: (100 - POKOK) => 100 - 64 = 36%
        val persenPokok = hitungPersen(totalPokok, totalKeluar)
        val persenBiasa = if (totalKeluar > 0.0) (100 - persenPokok).coerceIn(0, 100) else 0

        binding.progPrioritas.progress = persenPokok
        binding.lblPrioPokok.text = "Pokok / Prioritas ($persenPokok%)"
        binding.txtNomPrioPokok.text = "Rp ${formatAngka(totalPokok)}"
        binding.lblPrioBiasa.text = "Biasa ($persenBiasa%)"
        binding.txtNomPrioBiasa.text = "Rp ${formatAngka(totalBiasa)}"
    }

    private fun tampilkanKantong(listKeluar: List<TransaksiData>, totalKeluar: Double) {
        val kosong = listKeluar.isEmpty()
        binding.txtKosongKantong.visibility = if (kosong) View.VISIBLE else View.GONE
        binding.containerKantong.visibility = if (kosong) View.GONE else View.VISIBLE
        if (kosong) return

        val perKantong = mutableMapOf(
            "Tunai" to 0.0,
            "Bank BCA" to 0.0,
            "GoPay" to 0.0,
            "OVO" to 0.0,
            "ShopeePay" to 0.0
        )
        // Jumlahkan nominal ke 5 kantong utama. Contoh Bank BCA: (73.000 + 34.000) / 164.000 = 65%
        for (item in listKeluar) {
            if (perKantong.containsKey(item.kantong)) {
                perKantong[item.kantong] = perKantong.getValue(item.kantong) + item.nominal
            }
        }

        bindKantongRow("Tunai", perKantong.getValue("Tunai"), totalKeluar, binding.lblKanTunai, binding.txtNomTunai)
        bindKantongRow("Bank BCA", perKantong.getValue("Bank BCA"), totalKeluar, binding.lblKanBca, binding.txtNomBca)
        bindKantongRow("GoPay", perKantong.getValue("GoPay"), totalKeluar, binding.lblKanGopay, binding.txtNomGopay)
        bindKantongRow("OVO", perKantong.getValue("OVO"), totalKeluar, binding.lblKanOvo, binding.txtNomOvo)
        bindKantongRow("ShopeePay", perKantong.getValue("ShopeePay"), totalKeluar, binding.lblKanShopeepay, binding.txtNomShopeepay)
    }

    private fun bindKantongRow(
        nama: String,
        nilai: Double,
        total: Double,
        labelView: TextView,
        nominalView: TextView
    ) {
        val persen = hitungPersen(nilai, total)
        labelView.text = "$nama ($persen%)"
        nominalView.text = "Rp ${formatAngka(nilai)}"
    }

    private fun tampilkanTopPengeluaran(listKeluar: List<TransaksiData>) {
        // Urutkan nominal terbesar lalu ambil 3 teratas
        val terurut = listKeluar.sortedByDescending { it.nominal }.take(3)
        val kosong = terurut.isEmpty()
        binding.txtKosongTop.visibility = if (kosong) View.VISIBLE else View.GONE
        binding.divTop1.visibility = if (terurut.size > 1) View.VISIBLE else View.GONE
        binding.divTop2.visibility = if (terurut.size > 2) View.VISIBLE else View.GONE

        bindTopRow(1, terurut.getOrNull(0), binding.rowTop1, binding.txtTopJudul1, binding.txtTopSub1, binding.txtTopNom1)
        bindTopRow(2, terurut.getOrNull(1), binding.rowTop2, binding.txtTopJudul2, binding.txtTopSub2, binding.txtTopNom2)
        bindTopRow(3, terurut.getOrNull(2), binding.rowTop3, binding.txtTopJudul3, binding.txtTopSub3, binding.txtTopNom3)
    }

    private fun bindTopRow(
        peringkat: Int,
        item: TransaksiData?,
        rowView: View,
        judulView: TextView,
        subView: TextView,
        nominalView: TextView
    ) {
        if (item == null) {
            rowView.visibility = View.GONE
            rowView.setOnClickListener(null)
            return
        }
        rowView.visibility = View.VISIBLE
        judulView.text = "$peringkat. ${item.judul}"
        subView.text = "${item.kategori} · ${item.kantong} · ${item.tanggal}"
        nominalView.text = "-Rp ${formatAngka(item.nominal)}"
        rowView.setOnClickListener {
            val intent = Intent(it.context, DetailTransaksiActivity::class.java)
            intent.putExtra("DATA_TRANSAKSI", item)
            it.context.startActivity(intent)
        }
    }

    private fun tampilkanSumberMasuk(listMasuk: List<TransaksiData>, totalMasuk: Double) {
        val kosong = listMasuk.isEmpty()
        binding.txtKosongMasuk.visibility = if (kosong) View.VISIBLE else View.GONE
        binding.containerMasuk.visibility = if (kosong) View.GONE else View.VISIBLE
        if (kosong) return

        var totalGaji = 0.0
        var totalBonus = 0.0
        var totalLainnya = 0.0
        // Kategori => Gaji, Bonus, dan sisanya Lainnya. Total Uang Masuk = 10.500.000
        // Contoh: (Kategori / Total Uang Masuk) * 100
        // Gaji (Rp 10.000.000): (10.000.000 / 10.500.000) * 100 = 95,23% => 95%
        // Bonus (Rp 500.000): (500.000 / 10.500.000) * 100 = 4,76% => 5%
        // Lainnya (Rp 0): (0 / 10.500.000) * 100 = 0%
        for (item in listMasuk) {
            when (item.kategori) {
                "Gaji" -> totalGaji += item.nominal
                "Bonus" -> totalBonus += item.nominal
                else -> totalLainnya += item.nominal
            }
        }

        bindMasukRow("Gaji", totalGaji, totalMasuk, binding.lblMasukGaji, binding.txtNomMasukGaji)
        bindMasukRow("Bonus", totalBonus, totalMasuk, binding.lblMasukBonus, binding.txtNomMasukBonus)
        bindMasukRow("Lainnya", totalLainnya, totalMasuk, binding.lblMasukLainnya, binding.txtNomMasukLainnya)
    }

    private fun bindMasukRow(
        nama: String,
        nilai: Double,
        total: Double,
        labelView: TextView,
        nominalView: TextView
    ) {
        val persen = hitungPersen(nilai, total)
        labelView.text = "$nama ($persen%)"
        nominalView.text = "+Rp ${formatAngka(nilai)}"
    }
}
