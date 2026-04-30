package com.anggun.pos.model

import android.os.Parcel
import android.os.Parcelable

data class ModelProduk(
    val idProduk: String? = null,
    val namaProduk: String? = null,
    val hargaProduk: Int? = 0,
    val idKategori: String? = null,
    val idCabang: String? = null,
    val fotoProduk: String? = null,
    var stokProduk: Int? = 0,
    val tanpaBatas: Boolean? = false,
    val statusProduk: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
): Parcelable {

    var jumlahTerjual :Int = 0
        get() = field
        set(value) {field = value}

    constructor(parcel: Parcel) : this(
        idProduk = parcel.readString(),
        namaProduk = parcel.readString(),
        hargaProduk = parcel.readValue(Int::class.java.classLoader) as? Int,
        idKategori = parcel.readString(),
        idCabang = parcel.readString(),
        fotoProduk = parcel.readString(),
        stokProduk = parcel.readValue(Int::class.java.classLoader) as? Int,
        tanpaBatas = parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        statusProduk = parcel.readString(),
        createdAt = parcel.readString(),
        updatedAt = parcel.readString()
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeValue(hargaProduk)
        parcel.writeString(idKategori)
        parcel.writeString(idCabang)
        parcel.writeString(fotoProduk)
        parcel.writeValue(stokProduk)
        parcel.writeValue(tanpaBatas)
        parcel.writeString(statusProduk)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ModelProduk> {
        override fun createFromParcel(parcel: Parcel): ModelProduk {
            return ModelProduk(parcel)
        }

        override fun newArray(size: Int): Array<ModelProduk?> {
            return arrayOfNulls(size)
        }
    }
}