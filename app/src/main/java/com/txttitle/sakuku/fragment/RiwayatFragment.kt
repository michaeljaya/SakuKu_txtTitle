package com.txttitle.sakuku.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.txttitle.sakuku.R
import com.txttitle.sakuku.adapter.TransaksiAdapter
import com.txttitle.sakuku.data.TransaksiData
import com.txttitle.sakuku.databinding.FragmentRiwayatBinding
import org.json.JSONObject

class RiwayatFragment : Fragment() {

    private lateinit var binding: FragmentRiwayatBinding
    var listTransaksi: ArrayList<TransaksiData> = ArrayList()
    var filterTipe = "SEMUA"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRiwayatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Filter RadioGroup: Semua, Pengeluaran, Pemasukan
        binding.rgFilterRiwayat.setOnCheckedChangeListener { _, checkedId ->
            filterTipe = when (checkedId) {
                R.id.rbFilterPengeluaran -> "PENGELUARAN"
                R.id.rbFilterPemasukan -> "PEMASUKAN"
                else -> "SEMUA"
            }
            loadRiwayat()
        }
    }

    override fun onResume() {
        super.onResume()
        loadRiwayat()
    }

    private fun updateRiwayatList() {
        if (listTransaksi.isEmpty()) {
            binding.rvAllTransaksi.visibility = View.GONE
            binding.layoutEmptyRiwayat.visibility = View.VISIBLE
        } else {
            binding.rvAllTransaksi.visibility = View.VISIBLE
            binding.layoutEmptyRiwayat.visibility = View.GONE
            with(binding.rvAllTransaksi) {
                layoutManager = LinearLayoutManager(activity)
                setHasFixedSize(true)
                adapter = TransaksiAdapter(listTransaksi)
            }
        }
    }

    // Ambil riwayat transaksi dari endpoint PHP sesuai filter
    private fun loadRiwayat() {
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
                        listTransaksi = Gson().fromJson(data.toString(), sType)
                        updateRiwayatList()
                    }
                }
            },
            { it.printStackTrace() }
        ) {
            override fun getParams() = hashMapOf(
                "tipe" to filterTipe
            )
        }
        q.add(sr)
    }
}