package com.anggun.pos.produk

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anggun.pos.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class ModProdukActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("produk")

    private lateinit var etNamaProduk: TextInputEditText
    private lateinit var etHargaProduk: TextInputEditText
    private lateinit var etStok: TextInputEditText
    private lateinit var cbStokTakTerbatas: CheckBox
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnKamera: MaterialButton
    private lateinit var btnGaleri: MaterialButton
    private lateinit var btnPilihKategori: MaterialButton
    private lateinit var btnPilihCabang: MaterialButton
    private lateinit var tvJudul: TextView

    private var idProdukTerpilih: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)
        supportActionBar?.hide()

        init()
        setupListener()

        cbStokTakTerbatas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etStok.setText("0")
                etStok.isEnabled = false
            } else {
                etStok.isEnabled = true
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        tvJudul = findViewById(R.id.tvJudul)
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etHargaProduk = findViewById(R.id.etHargaProduk)
        etStok = findViewById(R.id.etStok)
        cbStokTakTerbatas = findViewById(R.id.cbStokTakTerbatas)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnKamera = findViewById(R.id.btnKamera)
        btnGaleri = findViewById(R.id.btnGaleri)
        btnPilihKategori = findViewById(R.id.btnPilihKategori)
        btnPilihCabang = findViewById(R.id.btnPilihCabang)
    }

    private fun setupListener() {
        btnSimpan.setOnClickListener {
            cekValidasi()
        }

        btnKamera.setOnClickListener {
            Toast.makeText(this, "Kamera", Toast.LENGTH_SHORT).show()
        }

        btnGaleri.setOnClickListener {
            Toast.makeText(this, "Galeri", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cekValidasi() {
        val nama = etNamaProduk.text.toString().trim()
        val harga = etHargaProduk.text.toString().trim()
        val stok = etStok.text.toString().trim()

        if (nama.isEmpty()) {
            etNamaProduk.error = "Wajib diisi"
            return
        }

        if (harga.isEmpty()) {
            etHargaProduk.error = "Wajib diisi"
            return
        }

        if (!cbStokTakTerbatas.isChecked && stok.isEmpty()) {
            etStok.error = "Wajib diisi"
            return
        }

        simpan(nama, harga.toLong(), stok)
    }

    private fun simpan(nama: String, harga: Long, stok: String) {
        val id = idProdukTerpilih ?: myRef.push().key!!

        val dataProduk = mapOf(
            "idProduk" to id,
            "namaProduk" to nama,
            "harga" to harga,
            "stok" to if (cbStokTakTerbatas.isChecked) "Unlimited" else stok,
            "kategori" to btnPilihKategori.text.toString(),
            "cabang" to btnPilihCabang.text.toString()
        )

        myRef.child(id).setValue(dataProduk)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}