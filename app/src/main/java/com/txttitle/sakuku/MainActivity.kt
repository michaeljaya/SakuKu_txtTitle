package com.txttitle.sakuku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.txttitle.sakuku.adapter.VIewPagerAdapter
import com.txttitle.sakuku.databinding.ActivityMainBinding
import com.txttitle.sakuku.fragment.AkunFragment
import com.txttitle.sakuku.fragment.AnalisisFragment
import com.txttitle.sakuku.fragment.BerandaFragment
import com.txttitle.sakuku.fragment.RiwayatFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragments: ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi fragment
        fragments.add(BerandaFragment())
        fragments.add(RiwayatFragment())
        fragments.add(AnalisisFragment())
        fragments.add(AkunFragment())

        // Setup viewpager
        binding.viewPager.adapter = VIewPagerAdapter(this, fragments)

        // Handle perubahan halaman
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menuId = binding.bottomNav.menu.getItem(position).itemId
                binding.bottomNav.selectedItemId = menuId
            }
        })

        // Handle klik bottom nav
        binding.bottomNav.setOnItemSelectedListener {
            binding.viewPager.currentItem = when (it.itemId) {
                R.id.itemBeranda -> 0
                R.id.itemRiwayat -> 1
                R.id.itemAnalisis -> 2
                R.id.itemAkun -> 3
                else -> 0
            }
            true
        }
    }
}
