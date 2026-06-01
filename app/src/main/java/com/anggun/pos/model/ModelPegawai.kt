package com.anggun.pos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelPegawai(
    var idPegawai: String? = null,
    var nama: String? = null,
     var alamat: String? = null,
    var role: String? = "Kasir",
    var foto: String? = null
) : Parcelable