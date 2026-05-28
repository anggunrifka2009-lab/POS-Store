package com.anggun.pos.cabang

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anggun.pos.R
import com.anggun.pos.model.ModelCabang
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class ModCabangActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    private lateinit var etNamaCabang: TextInputEditText
    private lateinit var etAlamatCabang: TextInputEditText
    private lateinit var actvStatusCabang: AutoCompleteTextView
    private lateinit var btnSimpan: MaterialButton
    private lateinit var ivKembali: ImageView
    private lateinit var tvJudul: TextView

    private var idCabangTerpilih: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_cabang)
        supportActionBar?.hide()

        init()
        setupDropdown()

        ivKembali.setOnClickListener {
            finish()
        }

        val dataIntent = intent.getParcelableExtra<ModelCabang>("DATA_CABANG")
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
        etNamaCabang = findViewById(R.id.etNamaCabang)
        etAlamatCabang = findViewById(R.id.etAlamatCabang)
        actvStatusCabang = findViewById(R.id.actvStatusCabang)
        btnSimpan = findViewById(R.id.btnSimpan)
        ivKembali = findViewById(R.id.ivKembali)
        tvJudul = findViewById(R.id.tvJudul)
    }

    private fun setupDropdown() {
        val statusArray = arrayOf("Aktif", "Tidak Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusArray)
        actvStatusCabang.setAdapter(adapter)
    }

    private fun setupModeEdit(cabang: ModelCabang) {
        idCabangTerpilih = cabang.idcabang
        etNamaCabang.setText(cabang.namaCabang)
        etAlamatCabang.setText(cabang.alamatCabang)
        actvStatusCabang.setText(cabang.status, false)
        tvJudul.text = "Ubah Cabang"
        btnSimpan.text = "Perbarui"
    }

    private fun cekValidasi() {
        val nama = etNamaCabang.text.toString().trim()
        val alamat = etAlamatCabang.text.toString().trim()
        val status = actvStatusCabang.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaCabang.error = "Nama Cabang Tidak Boleh Kosong"
            return
        }

        if (alamat.isEmpty()) {
            etAlamatCabang.error = "Alamat Cabang Tidak Boleh Kosong"
            return
        }

        if (status.isEmpty()) {
            Toast.makeText(this, "Pilih Status Cabang", Toast.LENGTH_SHORT).show()
            return
        }

        simpan(nama, alamat, status)
    }

    private fun simpan(nama: String, alamat: String, status: String) {
        val id = idCabangTerpilih ?: myRef.push().key!!

        val cabang = ModelCabang(
            idcabang = id,
            namaCabang = nama,
            alamatCabang = alamat,
            status = status
        )

        myRef.child(id).setValue(cabang)
            .addOnSuccessListener {
                val pesan =
                    if (idCabangTerpilih == null)
                        "Berhasil simpan"
                    else
                        "Berhasil update"

                Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}