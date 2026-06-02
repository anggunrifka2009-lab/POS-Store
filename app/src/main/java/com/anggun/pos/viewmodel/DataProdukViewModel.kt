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

        val kategoriRef = database.getReference("kategori")
        val cabangRef = database.getReference("cabang")

        kategoriRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(katSnapshot: DataSnapshot) {
                val activeCategories = HashSet<String>()
                for (kat in katSnapshot.children) {
                    val nama = kat.child("namaKategori").value?.toString()
                    val status = kat.child("status").value?.toString()
                    if (nama != null && status == "Aktif") {
                        activeCategories.add(nama)
                    }
                }

                cabangRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(cabSnapshot: DataSnapshot) {
                        val activeBranches = HashSet<String>()
                        activeBranches.add("Semua Cabang")
                        for (cab in cabSnapshot.children) {
                            val nama = cab.child("namaCabang").value?.toString()
                            val status = cab.child("status").value?.toString()
                            if (nama != null && status == "Aktif") {
                                activeBranches.add(nama)
                            }
                        }

                        myRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                isLoading.value = false
                                val list = ArrayList<ModelProduk>()

                                if (snapshot.exists()) {
                                    for (dataSnapshot in snapshot.children) {
                                        val produk = dataSnapshot.getValue(ModelProduk::class.java)
                                        if (produk != null) {
                                            // Cek apakah kategori DAN cabang produk tersebut aktif
                                            val isKatAktif = activeCategories.contains(produk.idKategori)
                                            val isCabAktif = activeBranches.contains(produk.idCabang)

                                            if (isKatAktif && isCabAktif) {
                                                list.add(produk)
                                            }
                                        }
                                    }

                                    originalProdukList.clear()
                                    originalProdukList.addAll(list)
                                    produkList.value = list
                                    isSearchEmpty.value = list.isEmpty()
                                } else {
                                    originalProdukList.clear()
                                    produkList.value = ArrayList()
                                    isSearchEmpty.value = true
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                isLoading.value = false
                            }
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        isLoading.value = false
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
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