package com.anggun.pos.model

data class ModelTransaksi(
    var idProduk: String? = "",
    var namaProduk: String? = "",
    var hargaProduk: Int? = 0,
    var stokProduk: Int? = 0,
    var tanpaBatas: Boolean? = false,
    var qty: Int = 0
)