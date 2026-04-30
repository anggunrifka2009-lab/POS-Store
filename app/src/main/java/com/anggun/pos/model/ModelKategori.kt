package com.anggun.pos.model

import android.R
import android.os.Parcel
import android.os.Parcelable
import java.sql.ClientInfoStatus

data class ModelKategori(
    var idKategori: String? = null,
    var namaKategori: String? = null,
    var status: String? = null
): Parcelable {
    constructor(parcel: Parcel): this(
        idKategori = parcel.readString(),
        namaKategori = parcel.readString(),
        status = parcel.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idKategori)
        parcel.writeString(namaKategori)
        parcel.writeString(status)
    }
    companion object CREATOR : Parcelable.Creator<ModelKategori> {
        override fun createFromParcel(source: Parcel): ModelKategori {
            return ModelKategori(source)
        }

        override fun newArray(size: Int): Array<ModelKategori?> {
            return arrayOfNulls(size)
        }
    }

}