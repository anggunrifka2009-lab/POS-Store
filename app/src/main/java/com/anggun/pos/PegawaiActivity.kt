package com.anggun.pos

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.adapter.PegawaiAdapter
import com.anggun.pos.model.ModelPegawai
import com.anggun.pos.viewmodel.DataPegawaiViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PegawaiActivity : AppCompatActivity() {

    private val viewModel: DataPegawaiViewModel by viewModels()
    private lateinit var rvPegawai: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var ivKembali: ImageView
    private lateinit var etSearchPegawai: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pegawai)
        supportActionBar?.hide()

        init()

        rvPegawai.layoutManager = LinearLayoutManager(this)
        rvPegawai.setHasFixedSize(true)

        fabTambah.setOnClickListener {
            startActivity(Intent(this, TambahPegawaiActivity::class.java))
        }

        ivKembali.setOnClickListener {
            finish()
        }

        viewModel.pegawaiList.observe(this) { list ->
            val adapter = PegawaiAdapter(list)
            rvPegawai.adapter = adapter

            adapter.setOnItemClickListener(object : PegawaiAdapter.OnItemClickListener {
                override fun onItemClick(pegawai: ModelPegawai) {
                    if (!pegawai.idPegawai.isNullOrBlank()) {
                        val options = arrayOf("Edit", "Hapus")
                        AlertDialog.Builder(this@PegawaiActivity)
                            .setTitle("Pilih Aksi")
                            .setItems(options) { _, which ->
                                when (which) {
                                    0 -> { // Edit
                                        val intent = Intent(this@PegawaiActivity, TambahPegawaiActivity::class.java)
                                        intent.putExtra("DATA_PEGAWAI", pegawai)
                                        startActivity(intent)
                                    }
                                    1 -> { // Hapus
                                        AlertDialog.Builder(this@PegawaiActivity)
                                            .setTitle("Hapus Pegawai")
                                            .setMessage("Apakah Anda yakin ingin menghapus ${pegawai.nama}?")
                                            .setPositiveButton("Ya") { _, _ ->
                                                viewModel.hapusPegawai(pegawai.idPegawai!!)
                                                Toast.makeText(this@PegawaiActivity, "Pegawai dihapus", Toast.LENGTH_SHORT).show()
                                            }
                                            .setNegativeButton("Tidak", null)
                                            .show()
                                    }
                                }
                            }
                            .show()
                    } else {
                        Toast.makeText(this@PegawaiActivity, "ID Kosong", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }

        etSearchPegawai.addTextChangedListener(object : TextWatcher {
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
        rvPegawai = findViewById(R.id.rvPegawai)
        fabTambah = findViewById(R.id.fabTambahPegawai)
        ivKembali = findViewById(R.id.ivKembali)
        etSearchPegawai = findViewById(R.id.etSearchPegawai)
    }
}