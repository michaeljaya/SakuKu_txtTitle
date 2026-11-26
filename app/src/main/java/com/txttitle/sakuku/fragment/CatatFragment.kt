package com.txttitle.sakuku.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.txttitle.sakuku.R
import com.txttitle.sakuku.databinding.FragmentCatatBinding
import org.json.JSONObject

class CatatFragment : Fragment() {

    private lateinit var binding: FragmentCatatBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup pilihan kategori
        val kategoriList = arrayOf(
            "Makan & Minum", "Transportasi", "Tagihan",
            "Belanja", "Kesehatan", "Gaji", "Bonus", "Lainnya"
        )
        val katAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kategoriList)
        katAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategori.adapter = katAdapter

        // Setup Pilihan Sumber Kantong
        val kantongList = arrayOf("Tunai", "Bank BCA", "GoPay", "OVO", "ShopeePay")
        val kantongAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kantongList)
        kantongAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerKantong.adapter = kantongAdapter

        // Handle tombol Simpan Transaksi
        binding.btnSimpan.setOnClickListener {
            val judul = binding.edtJudul.text.toString().trim()
            val nominalStr = binding.edtNominal.text.toString().trim()
            val nominal = nominalStr.toDoubleOrNull()

            if (judul.isEmpty()) {
                Toast.makeText(activity, "Judul transaksi wajib diisi!", Toast.LENGTH_SHORT).show()
            } else if (nominal == null || nominal <= 0) {
                Toast.makeText(activity, "Masukkan nominal yang valid!", Toast.LENGTH_SHORT).show()
            } else {
                val tipe = if (binding.radioPemasukan.isChecked) "PEMASUKAN" else "PENGELUARAN"
                val kategori = binding.spinnerKategori.selectedItem.toString()
                val kantong = binding.spinnerKantong.selectedItem.toString()
                val catatan = binding.edtCatatan.text.toString().trim()
                val isPrioritas = if (binding.checkPrioritas.isChecked) "1" else "0"

                simpanTransaksi(judul, nominalStr, tipe, kategori, kantong, catatan, isPrioritas)
            }
        }
    }

    // Kirim data transaksi baru ke tambah_transaksi.php via Volley POST
    private fun simpanTransaksi(
        judul: String,
        nominal: String,
        tipe: String,
        kategori: String,
        kantong: String,
        catatan: String,
        isPrioritas: String
    ) {
        val url = "http://10.0.2.2/sakuku/tambah_transaksi.php"
        val q = Volley.newRequestQueue(activity)
        val sr = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if (activity != null) {
                    val obj = JSONObject(response)
                    if (obj.getString("result") == "SUCCESS") {
                        Toast.makeText(activity, "Transaksi berhasil dicatat!", Toast.LENGTH_SHORT).show()
                        resetForm()
                        activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.itemRiwayat
                    } else {
                        Toast.makeText(activity, obj.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            {
                it.printStackTrace()
                if (activity != null) {
                    Toast.makeText(activity, "Gagal terhubung ke server!", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            override fun getParams() = hashMapOf(
                "judul" to judul,
                "nominal" to nominal,
                "tipe" to tipe,
                "kategori" to kategori,
                "kantong" to kantong,
                "catatan" to catatan,
                "is_prioritas" to isPrioritas
            )
        }
        q.add(sr)
    }

    private fun resetForm() {
        binding.radioPengeluaran.isChecked = true
        binding.edtJudul.setText("")
        binding.edtNominal.setText("")
        binding.edtCatatan.setText("")
        binding.spinnerKategori.setSelection(0)
        binding.spinnerKantong.setSelection(0)
        binding.checkPrioritas.isChecked = false
    }
}
