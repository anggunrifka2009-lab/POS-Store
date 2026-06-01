package com.anggun.pos.model

data class ModelRiwayat(
    val idTransaksi: String? = "",
    val kasir: String? = "",
    val cabang: String? = "",
    val alamat_cabang: String? = "",
    val tanggal: String? = "",
    val jam: String? = "",
    val total: Int? = 0,
    val bayar: Int? = 0
)
