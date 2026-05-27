package com.anggun.pos.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.R
import com.anggun.pos.adapter.KategoriAdapter
import com.anggun.pos.model.ModelKategori
import com.anggun.pos.viewmodel.DataKategoriViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataKategoriActivity : AppCompatActivity() {
    private val viewModel: DataKategoriViewModel by viewModels()
    private lateinit var rvKategori: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var ivKembali: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)
        supportActionBar?.hide()

        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvKategori.layoutManager = layoutManager
        rvKategori.setHasFixedSize(true)

        fabTambah.setOnClickListener {
            val intent = Intent(this, ModKategoriActivity::class.java)
            startActivity(intent)
        }

        ivKembali.setOnClickListener {
            finish()
        }

        viewModel.kategoriList.observe(this) { list ->
            val adapter = KategoriAdapter(list)
            rvKategori.adapter = adapter

            adapter.setOnItemClickListener(object : KategoriAdapter.OnItemClickListener {
                override fun onItemClick(kategori: ModelKategori) {
                    if (!kategori.idkategori.isNullOrBlank()) {
                        val intent = Intent(this@DataKategoriActivity, ModKategoriActivity::class.java)
                        intent.putExtra("DATA_KATEGORI", kategori)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@DataKategoriActivity, "ID Kosong", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        rvKategori = findViewById(R.id.rvKategori)
        fabTambah = findViewById(R.id.fabTambahk)
        ivKembali = findViewById(R.id.ivKembalik)
    }
}