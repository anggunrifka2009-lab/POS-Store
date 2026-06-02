package com.anggun.pos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anggun.pos.model.ModelPegawai
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataPegawaiViewModel : ViewModel() {

    private val dbRef = FirebaseDatabase.getInstance().getReference("pegawai")
    private val _pegawaiList = MutableLiveData<List<ModelPegawai>>()
    val pegawaiList: LiveData<List<ModelPegawai>> get() = _pegawaiList

    private var fullList = listOf<ModelPegawai>()

    init {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<ModelPegawai>()
                for (data in snapshot.children) {
                    val pegawai = data.getValue(ModelPegawai::class.java)
                    if (pegawai != null) {
                        items.add(pegawai)
                    }
                }
                fullList = items
                _pegawaiList.value = items
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun filterList(query: String) {
        if (query.isEmpty()) {
            _pegawaiList.value = fullList
        } else {
            val filtered = fullList.filter {
                it.nama?.contains(query, ignoreCase = true) == true ||
                it.alamat?.contains(query, ignoreCase = true) == true
            }
            _pegawaiList.value = filtered
        }
    }

    fun hapusPegawai(id: String) {
        dbRef.child(id).removeValue()
        // Juga hapus dari node akun
        FirebaseDatabase.getInstance().getReference("akun").child(id).removeValue()
    }
}