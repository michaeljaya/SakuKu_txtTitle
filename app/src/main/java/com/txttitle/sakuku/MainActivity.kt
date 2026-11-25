package com.txttitle.sakuku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.txttitle.sakuku.adapter.VIewPagerAdapter
import com.txttitle.sakuku.databinding.ActivityMainBinding
import com.txttitle.sakuku.fragment.AkunFragment
import com.txttitle.sakuku.fragment.AnalisisFragment
import com.txttitle.sakuku.fragment.BerandaFragment
import com.txttitle.sakuku.fragment.CatatFragment
import com.txttitle.sakuku.fragment.RiwayatFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val fragments: ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi 5 fragment sesuai urutan navbar
        fragments.add(BerandaFragment())
        fragments.add(RiwayatFragment())
        fragments.add(CatatFragment())
        fragments.add(AnalisisFragment())
        fragments.add(AkunFragment())

        // Setup viewpager
        binding.viewPager.adapter = VIewPagerAdapter(this, fragments)

        // Handle perubahan halaman (swipe)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menuId = binding.bottomNav.menu.getItem(position).itemId
                binding.bottomNav.selectedItemId = menuId

                if (position == 2) {
                    binding.btnNavCatat.setColorFilter(
                        ContextCompat.getColor(this@MainActivity, R.color.lime_accent)
                    )
                } else {
                    binding.btnNavCatat.setColorFilter(
                        ContextCompat.getColor(this@MainActivity, R.color.white)
                    )
                }
            }
        })

        // Handle klik bottom nav
        binding.bottomNav.setOnItemSelectedListener {
            binding.viewPager.currentItem = when (it.itemId) {
                R.id.itemBeranda -> 0
                R.id.itemRiwayat -> 1
                R.id.itemCatat -> 2
                R.id.itemAnalisis -> 3
                R.id.itemAkun -> 4
                else -> 0
            }
            true
        }

        // Handle klik tombol lingkaran tengah (+)
        binding.btnNavCatat.setOnClickListener {
            binding.viewPager.currentItem = 2
        }
    }
}
