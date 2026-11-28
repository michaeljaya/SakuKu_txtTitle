package com.txttitle.sakuku.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.txttitle.sakuku.fragment.AkunFragment
import com.txttitle.sakuku.fragment.AnalisisFragment
import com.txttitle.sakuku.fragment.BerandaFragment
import com.txttitle.sakuku.fragment.CatatFragment
import com.txttitle.sakuku.fragment.RiwayatFragment

class VIewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    // Buat instance fragment di adapter agar lifecycle FragmentStateAdapter tetap aman
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> BerandaFragment()
        1 -> RiwayatFragment()
        2 -> CatatFragment()
        3 -> AnalisisFragment()
        else -> AkunFragment()
    }
}
