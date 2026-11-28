package com.txttitle.sakuku

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.txttitle.sakuku.adapter.VIewPagerAdapter
import com.txttitle.sakuku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = VIewPagerAdapter(this)
        // Jaga view ke-5 tab tetap siap agar animasi geser mulus tanpa kedip kosong
        binding.viewPager.offscreenPageLimit = 4

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val menuId = binding.bottomNav.menu.getItem(position).itemId
                // Cegah loop balik dengan OnItemSelectedListener saat tab sudah sesuai
                if (binding.bottomNav.selectedItemId != menuId) {
                    binding.bottomNav.selectedItemId = menuId
                }

                val tintCatat = if (position == 2) R.color.pine_deep else R.color.nav_plus_inactive
                binding.btnNavCatat.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this@MainActivity, tintCatat)
                )
            }
        })

        binding.bottomNav.setOnItemSelectedListener {
            val target = when (it.itemId) {
                R.id.itemBeranda -> 0
                R.id.itemRiwayat -> 1
                R.id.itemCatat -> 2
                R.id.itemAnalisis -> 3
                R.id.itemAkun -> 4
                else -> 0
            }
            // Buka langsung hanya jika posisi tujuan belum aktif
            if (binding.viewPager.currentItem != target) {
                binding.viewPager.setCurrentItem(target, true)
            }
            true
        }

        // Arahkan tombol (+) tengah navbar ke tab Catat (indeks 2)
        binding.btnNavCatat.setOnClickListener {
            if (binding.viewPager.currentItem != 2) {
                binding.viewPager.setCurrentItem(2, true)
            }
        }
    }
}
