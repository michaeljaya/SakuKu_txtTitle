package com.txttitle.sakuku.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.txttitle.sakuku.DetailTransaksiActivity
import com.txttitle.sakuku.R
import com.txttitle.sakuku.data.TransaksiData
import com.txttitle.sakuku.databinding.CardTransaksiBinding

class TransaksiAdapter(val listTransaksi: ArrayList<TransaksiData>) :
    RecyclerView.Adapter<TransaksiAdapter.TransaksiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val binding = CardTransaksiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransaksiViewHolder(binding)
    }

    // Ini Untuk Menampilkan diBeranda Misal:
    // Kopi Kenangan
    // Makan & Minum . Metode . Tanggalnya
    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        val item = listTransaksi[position]
        val nominalBulat = item.nominal.toInt()

        with(holder.binding) {
            txtJudul.text = item.judul
            txtSubDetail.text = "${item.kategori} · ${item.kantong} · ${item.tanggal}"

            if (item.tipe == "PEMASUKAN") {
                txtNominal.text = "+Rp $nominalBulat"
                txtNominal.setTextColor(root.context.getColor(R.color.income_green))
            } else {
                txtNominal.text = "-Rp $nominalBulat"
                txtNominal.setTextColor(root.context.getColor(R.color.expense_rose))
            }

            // Klik card untuk membuka halaman detail transaksi
            root.setOnClickListener {
                val intent = Intent(it.context, DetailTransaksiActivity::class.java)
                intent.putExtra("DATA_TRANSAKSI", item)
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = listTransaksi.size

    class TransaksiViewHolder(val binding: CardTransaksiBinding) :
        RecyclerView.ViewHolder(binding.root)
}
