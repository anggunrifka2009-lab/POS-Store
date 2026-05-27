package com.anggun.pos.produk

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.adapter.FilterKategoriAdapter
import com.anggun.pos.adapter.ProdukAdapter
import com.anggun.pos.viewmodel.DataProdukViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase

class DataProdukActivity : AppCompatActivity() {

    private lateinit var rvProduk: RecyclerView
    private lateinit var rvKategoriFilter: RecyclerView

    private lateinit var fabTambah: FloatingActionButton
    private lateinit var etSearch: EditText
    private lateinit var ivKembalip: ImageView

    private val viewModel: DataProdukViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_produk)

        supportActionBar?.hide()

        init()

        rvProduk.layoutManager =
            LinearLayoutManager(this)

        rvProduk.setHasFixedSize(true)

        rvKategoriFilter.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvKategoriFilter.setHasFixedSize(true)

        ambilKategori()

        fabTambah.setOnClickListener {

            val intent =
                Intent(this, ModProdukActivity::class.java)

            startActivity(intent)
        }

        ivKembalip.setOnClickListener {

            finish()
        }

        etSearch.addTextChangedListener {

            viewModel.filterList(it.toString())
        }

        viewModel.produkList.observe(this) { list ->

            val adapter =
                ProdukAdapter(list)

            rvProduk.adapter = adapter
        }
    }

    private fun init() {

        rvProduk =
            findViewById(R.id.rvProduk)

        rvKategoriFilter =
            findViewById(R.id.rvKategoriFilter)

        fabTambah =
            findViewById(R.id.fabTambahp)

        etSearch =
            findViewById(R.id.etSearchPro)

        ivKembalip =
            findViewById(R.id.ivKembalip)
    }

    private fun ambilKategori() {

        val kategoriRef =
            FirebaseDatabase.getInstance()
                .getReference("kategori")

        kategoriRef.get()
            .addOnSuccessListener { snapshot ->

                val listKategori =
                    ArrayList<String>()

                listKategori.add("Semua")

                for (data in snapshot.children) {

                    val namaKategori =
                        data.child("namaKategori")
                            .value.toString()

                    val status =
                        data.child("status")
                            .value.toString()

                    if (
                        namaKategori.isNotEmpty() &&
                        status == "Aktif"
                    ) {

                        listKategori.add(namaKategori)
                    }
                }

                val adapter =
                    FilterKategoriAdapter(listKategori)

                rvKategoriFilter.adapter =
                    adapter

                adapter.setOnItemClickListener(object :
                    FilterKategoriAdapter.OnItemClickListener {

                    override fun onItemClick(
                        kategori: String
                    ) {

                        if (kategori == "Semua") {

                            viewModel.getData()

                        } else {

                            viewModel.filterKategori(
                                kategori
                            )
                        }
                    }
                })
            }
    }
}