package com.txttitle.sakuku.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.txttitle.sakuku.databinding.FragmentCatatBinding

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
    }
}
