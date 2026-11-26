package com.txttitle.sakuku.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.txttitle.sakuku.data.RingkasanData
import com.txttitle.sakuku.data.TransaksiData
import com.txttitle.sakuku.databinding.FragmentAnalisisBinding
import org.json.JSONObject

class AnalisisFragment : Fragment() {

    private lateinit var binding: FragmentAnalisisBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalisisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadRingkasanAnalisis()
        loadKomposisiKategori()
    }

    private fun loadRingkasanAnalisis() {
        val url = "http://10.0.2.2/sakuku/get_ringkasan.php"
        val q = Volley.newRequestQueue(activity)
        val sr = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                if (activity != null) {
                    val obj = JSONObject(response)
                    if (obj.getString("result") == "SUCCESS") {
                        val data = obj.getJSONObject("data")
                        val ringkasan = Gson().fromJson(data.toString(), RingkasanData::class.java)

                        binding.txtAnalisisMasuk.text = "+Rp ${ringkasan.total_masuk.toInt()}"
                        binding.txtAnalisisKeluar.text = "-Rp ${ringkasan.total_keluar.toInt()}"
                    }
                }
            },
            { it.printStackTrace() }
        )
        q.add(sr)
    }

    private fun loadKomposisiKategori() {
        val url = "http://10.0.2.2/sakuku/get_transaksi.php"
        val q = Volley.newRequestQueue(activity)
        val sr = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if (activity != null) {
                    val obj = JSONObject(response)
                    if (obj.getString("result") == "SUCCESS") {
                        val data = obj.getJSONArray("data")
                        val sType = object : TypeToken<List<TransaksiData>>() {}.type
                        val listKeluar = Gson().fromJson<ArrayList<TransaksiData>>(data.toString(), sType)

                        var totalKeluar = 0.0
                        var totalMakan = 0.0
                        var totalTagihan = 0.0
                        var totalTransport = 0.0

                        for (item in listKeluar) {
                            totalKeluar += item.nominal
                            when (item.kategori) {
                                "Makan & Minum" -> totalMakan += item.nominal
                                "Tagihan" -> totalTagihan += item.nominal
                                "Transportasi" -> totalTransport += item.nominal
                            }
                        }

                        val pMakan = if (totalKeluar > 0) ((totalMakan / totalKeluar) * 100).toInt() else 0
                        val pTagihan = if (totalKeluar > 0) ((totalTagihan / totalKeluar) * 100).toInt() else 0
                        val pTransport = if (totalKeluar > 0) ((totalTransport / totalKeluar) * 100).toInt() else 0

                        binding.lblKatMakan.text = "Makan & Minum ($pMakan%)"
                        binding.progMakan.max = 100
                        binding.progMakan.progress = pMakan

                        binding.lblKatTagihan.text = "Tagihan Rutin & Listrik ($pTagihan%)"
                        binding.progTagihan.max = 100
                        binding.progTagihan.progress = pTagihan

                        binding.lblKatTransport.text = "Transportasi ($pTransport%)"
                        binding.progTransport.max = 100
                        binding.progTransport.progress = pTransport
                    }
                }
            },
            { it.printStackTrace() }
        ) {
            override fun getParams() = hashMapOf(
                "tipe" to "PENGELUARAN"
            )
        }
        q.add(sr)
    }
}