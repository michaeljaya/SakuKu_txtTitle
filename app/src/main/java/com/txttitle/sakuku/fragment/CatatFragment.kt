package com.txttitle.sakuku.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.txttitle.sakuku.R
import com.txttitle.sakuku.databinding.FragmentCatatBinding
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class CatatFragment : Fragment() {

    private lateinit var binding: FragmentCatatBinding
    private var requestQueue: RequestQueue? = null
    private var sedangFormatNominal = false

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
        katAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerKategori.adapter = katAdapter

        // Setup Pilihan Sumber Kantong
        val kantongList = arrayOf("Tunai", "Bank BCA", "GoPay", "OVO", "ShopeePay")
        val kantongAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, kantongList)
        kantongAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerKantong.adapter = kantongAdapter

        // Format pemisah ribuan titik saat mengetik nominal
        binding.edtNominal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (sedangFormatNominal) return
                sedangFormatNominal = true

                val teksAsli = s?.toString() ?: ""
                val kursor = binding.edtNominal.selectionStart.coerceAtLeast(0)
                val jumlahDigitSebelumKursor = teksAsli.take(kursor).count { it.isDigit() }
                val digitSaja = teksAsli.filter { it.isDigit() }

                if (digitSaja.isEmpty()) {
                    binding.edtNominal.setText("")
                } else {
                    val nilai = digitSaja.toLongOrNull()
                    if (nilai != null) {
                        val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
                        formatter.maximumFractionDigits = 0
                        val hasil = formatter.format(nilai)

                        if (hasil != teksAsli) {
                            binding.edtNominal.setText(hasil)
                            // Cari posisi kursor yang sesuai dengan jumlah digit sebelum kursor
                            var posisiBaru = 0
                            var hitunganDigit = 0
                            while (posisiBaru < hasil.length && hitunganDigit < jumlahDigitSebelumKursor) {
                                if (hasil[posisiBaru].isDigit()) hitunganDigit++
                                posisiBaru++
                            }
                            binding.edtNominal.setSelection(posisiBaru)
                        }
                    }
                }

                sedangFormatNominal = false
            }
        })

        // Handle tombol Simpan Transaksi
        binding.btnSimpan.setOnClickListener {
            val judul = binding.edtJudul.text.toString().trim()
            val nominalStr = binding.edtNominal.text.toString().replace(".", "").trim()
            val nominal = nominalStr.toDoubleOrNull()

            if (judul.isEmpty()) {
                Toast.makeText(context, "Judul transaksi wajib diisi!", Toast.LENGTH_SHORT).show()
            } else if (nominal == null || nominal <= 0) {
                Toast.makeText(context, "Masukkan nominal yang valid!", Toast.LENGTH_SHORT).show()
            } else {
                val tipe = if (binding.radioPemasukan.isChecked) "PEMASUKAN" else "PENGELUARAN"
                val kategori = binding.spinnerKategori.selectedItem?.toString() ?: "Lainnya"
                val kantong = binding.spinnerKantong.selectedItem?.toString() ?: "Tunai"
                val catatan = binding.edtCatatan.text.toString().trim()
                val isPrioritas = if (binding.checkPrioritas.isChecked) "1" else "0"

                simpanTransaksi(judul, nominalStr, tipe, kategori, kantong, catatan, isPrioritas)
            }
        }
    }

    override fun onDestroyView() {
        requestQueue?.cancelAll("CATAT")
        super.onDestroyView()
    }

    private fun getQueue(): RequestQueue? {
        val appCtx = context?.applicationContext ?: return null
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(appCtx)
        }
        return requestQueue
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
        val q = getQueue() ?: return
        binding.btnSimpan.isEnabled = false
        val url = "http://10.0.2.2/sakuku/tambah_transaksi.php"
        val sr = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                if (isAdded && view != null) {
                    binding.btnSimpan.isEnabled = true
                    try {
                        val obj = JSONObject(response)
                        if (obj.getString("result") == "SUCCESS") {
                            Toast.makeText(context, "Transaksi berhasil dicatat!", Toast.LENGTH_SHORT).show()
                            resetForm()
                            activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.itemRiwayat
                        } else {
                            Toast.makeText(context, obj.optString("message", "Gagal menyimpan data!"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Respons server tidak valid!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            {
                it.printStackTrace()
                if (isAdded && view != null) {
                    binding.btnSimpan.isEnabled = true
                    Toast.makeText(context, "Gagal terhubung ke server!", Toast.LENGTH_SHORT).show()
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
        sr.setShouldCache(false)
        sr.retryPolicy = DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        sr.tag = "CATAT"
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
