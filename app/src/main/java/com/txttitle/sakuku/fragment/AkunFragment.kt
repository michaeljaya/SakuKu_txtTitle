package com.txttitle.sakuku.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.txttitle.sakuku.databinding.FragmentAkunBinding
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class AkunFragment : Fragment() {

    private lateinit var binding: FragmentAkunBinding
    private var requestQueue: RequestQueue? = null

    private val PREF_NAME = "SakuKuPreferences"
    private val KEY_USER_NAME = "user_display_name"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAkunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Akses SharedPreferences
        val shared = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Nilai bawaan aplikasi: Nama kosong ("")
        val savedName = shared.getString(KEY_USER_NAME, "") ?: ""
        tampilkanNama(savedName)

        // Klik ikon pensil untuk menampilkan/menyembunyikan form ubah nama
        binding.btnEditNama.setOnClickListener {
            if (binding.cardEditProfile.visibility == View.VISIBLE) {
                binding.cardEditProfile.visibility = View.GONE
            } else {
                binding.cardEditProfile.visibility = View.VISIBLE
                val currentName = shared.getString(KEY_USER_NAME, "") ?: ""
                binding.edtUserName.setText(currentName)
                binding.edtUserName.requestFocus()
            }
        }

        // Tombol Batal
        binding.btnBatalEdit.setOnClickListener {
            binding.cardEditProfile.visibility = View.GONE
        }

        // Tombol Simpan Nama
        binding.btnSimpanProfil.setOnClickListener {
            val inputName = binding.edtUserName.text.toString().trim()

            if (inputName.isEmpty()) {
                Toast.makeText(context, "Silakan masukkan nama Anda!", Toast.LENGTH_SHORT).show()
            } else {
                // Simpan nama baru ke SharedPreferences
                shared.edit().putString(KEY_USER_NAME, inputName).apply()

                // Update tampilan dan sembunyikan form
                tampilkanNama(inputName)
                binding.cardEditProfile.visibility = View.GONE
                Toast.makeText(context, "Nama profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestQueue?.cancelAll("AKUN")
        muatStatistik()
    }

    override fun onDestroyView() {
        requestQueue?.cancelAll("AKUN")
        super.onDestroyView()
    }

    private fun getQueue(): RequestQueue? {
        val appCtx = context?.applicationContext ?: return null
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(appCtx)
        }
        return requestQueue
    }

    // Mengambil total transaksi via Volley
    private fun muatStatistik() {
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
                            val data = obj.getJSONObject("data")
                            val totalTransaksi = data.optInt("total_transaksi", 0)
                            val formatJumlah = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
                            binding.txtTotalCatatan.text = "${formatJumlah.format(totalTransaksi)} Catatan"
                        } else {
                            binding.txtTotalCatatan.text = "0 Catatan"
                        }
                    } catch (e: Exception) {
                        binding.txtTotalCatatan.text = "0 Catatan"
                    }
                }
            },
            {
                it.printStackTrace()
                if (isAdded && view != null) {
                    binding.txtTotalCatatan.text = "0 Catatan"
                }
            }
        )
        sr.setShouldCache(false)
        sr.retryPolicy = DefaultRetryPolicy(3000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        sr.tag = "AKUN"
        q.add(sr)
    }

    private fun tampilkanNama(name: String) {
        if (name.isEmpty()) {
            binding.txtDisplayName.text = "Nama Belum Diatur"
            binding.edtUserName.setText("")
        } else {
            binding.txtDisplayName.text = name
            binding.edtUserName.setText(name)
        }
    }
}
