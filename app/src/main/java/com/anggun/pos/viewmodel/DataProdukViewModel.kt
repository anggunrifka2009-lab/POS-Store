package com.anggun.pos.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anggun.pos.model.ModelProduk
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataProdukViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("Produk")

    val produkList = MutableLiveData<ArrayList<ModelProduk>>()

    private var originalProdukList = ArrayList<ModelProduk>()

    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    init {
        getData()
    }

    fun getData() {

        isLoading.value = true

        myRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                isLoading.value = false

                val list = ArrayList<ModelProduk>()

                if (snapshot.exists()) {

                    for (dataSnapshot in snapshot.children) {

                        val produk =
                            dataSnapshot.getValue(
                                ModelProduk::class.java
                            )

                        if (produk != null) {

                            list.add(produk)
                        }
                    }

                    originalProdukList.clear()
                    originalProdukList.addAll(list)

                    produkList.value = list

                    isSearchEmpty.value = list.isEmpty()

                    Log.d(
                        "DataProdukViewModel",
                        "Jumlah produk: ${list.size}"
                    )

                } else {

                    originalProdukList.clear()

                    produkList.value = ArrayList()

                    isSearchEmpty.value = true
                }
            }

            override fun onCancelled(error: DatabaseError) {

                isLoading.value = false

                Log.e(
                    "DataProdukViewModel",
                    error.message
                )
            }
        })
    }

    fun filterList(query: String?) {

        if (query.isNullOrEmpty()) {

            produkList.value = originalProdukList

        } else {

            val filteredList =
                originalProdukList.filter {

                    it.namaProduk
                        ?.lowercase()
                        ?.contains(query.lowercase()) == true
                }

            produkList.value =
                ArrayList(filteredList)
        }
    }

    fun filterKategori(kategori: String) {

        if (kategori == "Semua") {

            produkList.value = originalProdukList

        } else {

            val filteredList =
                originalProdukList.filter {

                    it.idKategori == kategori
                }

            produkList.value =
                ArrayList(filteredList)
        }
    }
}