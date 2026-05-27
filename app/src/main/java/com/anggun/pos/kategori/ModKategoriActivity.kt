package com.anggun.pos.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anggun.pos.R
import com.anggun.pos.model.ModelKategori
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var actvStatusKategori: AutoCompleteTextView
    private lateinit var btnSimpan: MaterialButton
    private lateinit var ivKembali: ImageView

    private var idKategoriTerpilih: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)
        supportActionBar?.hide()

        init()
        setupDropdown()

        ivKembali.setOnClickListener {
            finish()
        }

        val dataIntent = intent.getParcelableExtra<ModelKategori>("DATA_KATEGORI")
        if (dataIntent != null) {
            setupModeEdit(dataIntent)
        }

        btnSimpan.setOnClickListener {
            cekValidasi()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        etNamaKategori = findViewById(R.id.etNamaKategori)
        actvStatusKategori = findViewById(R.id.actvStatusKategori)
        btnSimpan = findViewById(R.id.btnSimpan)
        ivKembali = findViewById(R.id.ivKembali)
    }

    private fun setupDropdown() {
        val statusArray = arrayOf("Aktif", "Non-Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusArray)
        actvStatusKategori.setAdapter(adapter)
    }

    private fun setupModeEdit(kategori: ModelKategori) {
        idKategoriTerpilih = kategori.idkategori
        etNamaKategori.setText(kategori.namaKategori)
        actvStatusKategori.setText(kategori.status, false)
        btnSimpan.text = "Perbarui"
    }

    private fun cekValidasi() {
        val nama = etNamaKategori.text.toString().trim()
        val status = actvStatusKategori.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaKategori.error = "Nama Kategori Tidak Boleh Kosong"
            return
        }

        if (status.isEmpty()) {
            Toast.makeText(this, "Pilih Status Kategori", Toast.LENGTH_SHORT).show()
            return
        }

        simpan(nama, status)
    }

    private fun simpan(nama: String, status: String) {
        val id = idKategoriTerpilih ?: myRef.push().key!!

        val kategori = ModelKategori(
            idkategori = id,
            namaKategori = nama,
            status = status
        )

        myRef.child(id).setValue(kategori)
            .addOnSuccessListener {

                val pesan =
                    if (idKategoriTerpilih == null)
                        "Berhasil simpan"
                    else
                        "Berhasil update"

                Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}