package com.txttitle.sakuku.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.txttitle.sakuku.CatatTransaksiActivity
import com.txttitle.sakuku.R
import com.txttitle.sakuku.databinding.FragmentBerandaBinding

class BerandaFragment : Fragment() {

    private lateinit var binding: FragmentBerandaBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FAB Tambah Transaksi
        binding.fabTambah.setOnClickListener {
            val intent = Intent(activity, CatatTransaksiActivity::class.java)
            startActivity(intent)
        }

        // Teks "Lihat Semua" pindah ke tab Riwayat
        binding.txtLihatSemua.setOnClickListener {
            activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.itemRiwayat
        }
    }

    override fun onResume() {
        super.onResume()
        updateGreeting()
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
}
