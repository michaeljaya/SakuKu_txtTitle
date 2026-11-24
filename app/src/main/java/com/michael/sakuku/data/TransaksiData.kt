package com.michael.sakuku.data

import java.io.Serializable

data class TransaksiData(
    val id: Int,
    val judul: String,
    val nominal: Double,
    val tipe: String,
    val kategori: String,
    val kantong: String,
    val tanggal: String,
    val catatan: String?,
    val is_prioritas: Int
) : Serializable
