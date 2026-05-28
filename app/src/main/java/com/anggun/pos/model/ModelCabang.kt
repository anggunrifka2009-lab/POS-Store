package com.anggun.pos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelCabang(
    var idcabang: String? = null,
    var namaCabang: String? = null,
    var alamatCabang: String? = null,
    var status: String? = null
) : Parcelable