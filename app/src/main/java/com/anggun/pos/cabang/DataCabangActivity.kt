package com.anggun.pos.cabang

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
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
import com.anggun.pos.adapter.CabangAdapter
import com.anggun.pos.model.ModelCabang
import com.anggun.pos.viewmodel.DataCabangViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataCabangActivity : AppCompatActivity() {
    private val viewModel: DataCabangViewModel by viewModels()
    private lateinit var rvCabang: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var ivKembali: ImageView
    private lateinit var etSearchCabang: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_cabang)
        supportActionBar?.hide()

        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvCabang.layoutManager = layoutManager
        rvCabang.setHasFixedSize(true)

        fabTambah.setOnClickListener {
            val intent = Intent(this, ModCabangActivity::class.java)
            startActivity(intent)
        }

        ivKembali.setOnClickListener {
            finish()
        }

        viewModel.cabangList.observe(this) { list ->
            val adapter = CabangAdapter(list)
            rvCabang.adapter = adapter
            adapter.appContext = applicationContext

            adapter.setOnItemClickListener(object : CabangAdapter.OnItemClickListener {
                override fun onItemClick(cabang: ModelCabang) {
                    if (!cabang.idcabang.isNullOrBlank()) {
                        val options = arrayOf("Edit", "Hapus")
                        androidx.appcompat.app.AlertDialog.Builder(this@DataCabangActivity)
                            .setTitle("Pilih Aksi")
                            .setItems(options) { _, which ->
                                when (which) {
                                    0 -> {
                                        val intent = Intent(this@DataCabangActivity, ModCabangActivity::class.java)
                                        intent.putExtra("DATA_CABANG", cabang)
                                        startActivity(intent)
                                    }
                                    1 -> {
                                        androidx.appcompat.app.AlertDialog.Builder(this@DataCabangActivity)
                                            .setTitle("Hapus Cabang")
                                            .setMessage("Apakah Anda yakin ingin menghapus ${cabang.namaCabang}?")
                                            .setPositiveButton("Ya") { _, _ ->
                                                viewModel.hapusCabang(cabang.idcabang!!)
                                                Toast.makeText(this@DataCabangActivity, "Cabang dihapus", Toast.LENGTH_SHORT).show()
                                            }
                                            .setNegativeButton("Tidak", null)
                                            .show()
                                    }
                                }
                            }
                            .show()
                    } else {
                        Toast.makeText(this@DataCabangActivity, "ID Kosong", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        etSearchCabang.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        rvCabang = findViewById(R.id.rvCabang)
        fabTambah = findViewById(R.id.fabTambah)
        ivKembali = findViewById(R.id.ivKembalic)
        etSearchCabang = findViewById(R.id.etSearchCabang)
    }
}