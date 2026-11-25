package com.txttitle.sakuku.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.txttitle.sakuku.databinding.FragmentAnalisisBinding

class AnalisisFragment : Fragment() {

    private lateinit var binding: FragmentAnalisisBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalisisBinding.inflate(inflater, container, false)
        return binding.root
    }
}
