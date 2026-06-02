package com.anggun.pos.model

data class ModelTransaksi(
    var idProduk: String? = "",
    var namaProduk: String? = "",
    var hargaProduk: Int? = 0,
    var stokProduk: Int? = 0,
    var tanpaBatas: Boolean? = false,
    var idKategori: String? = "",
    var idCabang: String? = "",
    var qty: Int = 0
)