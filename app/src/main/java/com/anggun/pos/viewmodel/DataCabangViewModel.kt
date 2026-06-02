package com.anggun.pos.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.anggun.pos.model.ModelCabang
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataCabangViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")
    val cabangList = MutableLiveData<ArrayList<ModelCabang>>()
    private var originalCabangList = ArrayList<ModelCabang>()
    private val searchQuery = MutableLiveData<String?>()
    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    init {
        getData()
    }

    fun getData() {
        isLoading.value = true
        val query = myRef.orderByChild("idcabang").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                if (snapshot.exists()) {
                    val list = ArrayList<ModelCabang>()
                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(ModelCabang::class.java)
                        if (cabang == null) {
                            Log.e("DataCabangViewModel", "Failed to parse cabang data for snapshot: ${dataSnapshot.key}")
                        } else {
                            list.add(cabang)
                        }
                    }
                    originalCabangList.clear()
                    originalCabangList.addAll(list)
                    cabangList.value = list
                    isSearchEmpty.value = false
                    Log.d("DataCabangViewModel", "Loaded ${list.size} cabang items.")
                } else {
                    originalCabangList.clear()
                    cabangList.value = ArrayList()
                    isSearchEmpty.value = true
                    Log.d("DataCabangViewModel", "No cabang data found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
            }
        })
    }

    fun filterList(query: String?) {
        searchQuery.value = query
        if (query.isNullOrEmpty()) {
            cabangList.value = originalCabangList
            isSearchEmpty.value = false
        } else {
            val filteredList = originalCabangList.filter {
                it.namaCabang?.lowercase()?.contains(query.lowercase()) == true
            }
            cabangList.value = ArrayList(filteredList)
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }

    fun hapusCabang(id: String) {
        myRef.child(id).removeValue()
    }
}