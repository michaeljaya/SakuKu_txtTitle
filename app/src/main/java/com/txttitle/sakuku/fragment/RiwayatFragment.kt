package com.txttitle.sakuku.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
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
    private var requestQueue: RequestQueue? = null

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

    override fun onDestroyView() {
        requestQueue?.cancelAll("RIWAYAT")
        super.onDestroyView()
    }

    private fun getQueue(): RequestQueue? {
        val appCtx = context?.applicationContext ?: return null
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(appCtx)
        }
        return requestQueue
    }

    private fun updateRiwayatList() {
        if (!isAdded || view == null) return
        if (listTransaksi.isEmpty()) {
            binding.rvAllTransaksi.visibility = View.GONE
            binding.layoutEmptyRiwayat.visibility = View.VISIBLE
        } else {
            binding.rvAllTransaksi.visibility = View.VISIBLE
            binding.layoutEmptyRiwayat.visibility = View.GONE
            with(binding.rvAllTransaksi) {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                adapter = TransaksiAdapter(listTransaksi)
            }
        }
    }

    // Ambil riwayat transaksi dari endpoint PHP sesuai filter
    private fun loadRiwayat() {
        val q = getQueue() ?: return
        val url = "http://10.0.2.2/sakuku/get_transaksi.php"
        val sr = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if (isAdded && view != null) {
                    try {
                        val obj = JSONObject(response)
                        if (obj.getString("result") == "SUCCESS") {
                            val data = obj.getJSONArray("data")
                            val sType = object : TypeToken<List<TransaksiData>>() {}.type
                            listTransaksi = Gson().fromJson(data.toString(), sType)
                            updateRiwayatList()
                        } else {
                            updateRiwayatList()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateRiwayatList()
                    }
                }
            },
            {
                it.printStackTrace()
                if (isAdded && view != null) {
                    updateRiwayatList()
                }
            }
        ) {
            override fun getParams() = hashMapOf(
                "tipe" to filterTipe
            )
        }
        sr.setShouldCache(false)
        sr.retryPolicy = DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        sr.tag = "RIWAYAT"
        q.add(sr)
    }
}
