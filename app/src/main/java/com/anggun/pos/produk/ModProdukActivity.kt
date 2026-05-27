package com.anggun.pos.produk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anggun.pos.R
import com.anggun.pos.model.ModelProduk
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ModProdukActivity : AppCompatActivity() {

    private lateinit var ivKembali: ImageView

    private lateinit var btnKamera: Button
    private lateinit var btnGaleri: Button

    private lateinit var imgPreview: ImageView

    private lateinit var etNamaProduk: TextInputEditText
    private lateinit var etHargaProduk: TextInputEditText
    private lateinit var etStok: TextInputEditText

    private lateinit var actvStatusProduk: AutoCompleteTextView

    private lateinit var cbStokTakTerbatas: CheckBox

    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnPilihKategori: MaterialButton
    private lateinit var btnPilihCabang: MaterialButton

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("Produk")

    private var kategoriDipilih = ""
    private var cabangDipilih = "Cabang Utama"

    private var imageUri: Uri? = null
    private var bitmapFoto: Bitmap? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    // ================= GALERI =================

    private val galeriLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                imageUri = result.data?.data

                imgPreview.setImageURI(imageUri)
            }
        }

    // ================= KAMERA =================

    private val kameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->

            if (bitmap != null) {

                bitmapFoto = bitmap

                imgPreview.setImageBitmap(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_produk)

        supportActionBar?.hide()

        init()

        setupDropdownStatus()

        ambilKategori()

        ivKembali.setOnClickListener {

            finish()
        }

        // ================= BUTTON KAMERA =================

        btnKamera.setOnClickListener {

            cekPermissionKamera()
        }

        // ================= BUTTON GALERI =================

        btnGaleri.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )

            galeriLauncher.launch(intent)
        }

        // ================= PILIH CABANG =================

        btnPilihCabang.setOnClickListener {

            val daftarCabang = arrayOf(
                "Cabang Utama",
                "Cabang Barat",
                "Cabang Timur"
            )

            AlertDialog.Builder(this)
                .setTitle("Pilih Cabang")
                .setItems(daftarCabang) { _, which ->

                    cabangDipilih =
                        daftarCabang[which]

                    btnPilihCabang.text =
                        cabangDipilih
                }
                .show()
        }

        // ================= SIMPAN =================

        btnSimpan.setOnClickListener {

            validasi()
        }
    }

    private fun init() {

        ivKembali =
            findViewById(R.id.ivKembali)

        btnKamera =
            findViewById(R.id.btnKamera)

        btnGaleri =
            findViewById(R.id.btnGaleri)

        imgPreview =
            findViewById(R.id.imgProduk)

        etNamaProduk =
            findViewById(R.id.etNamaProduk)

        etHargaProduk =
            findViewById(R.id.etHargaProduk)

        etStok =
            findViewById(R.id.etStok)

        actvStatusProduk =
            findViewById(R.id.actvStatusProduk)

        cbStokTakTerbatas =
            findViewById(R.id.cbStokTakTerbatas)

        btnSimpan =
            findViewById(R.id.btnSimpan)

        btnPilihKategori =
            findViewById(R.id.btnPilihKategori)

        btnPilihCabang =
            findViewById(R.id.btnPilihCabang)
    }

    // ================= DROPDOWN STATUS =================

    private fun setupDropdownStatus() {

        val statusArray = arrayOf(
            "Aktif",
            "Nonaktif"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            statusArray
        )

        actvStatusProduk.setAdapter(adapter)
    }

    // ================= AMBIL KATEGORI FIREBASE =================

    private fun ambilKategori() {

        val kategoriRef =
            FirebaseDatabase.getInstance()
                .getReference("kategori")

        kategoriRef.get()
            .addOnSuccessListener { snapshot ->

                val listKategori = ArrayList<String>()

                for (data in snapshot.children) {

                    val namaKategori =
                        data.child("namaKategori")
                            .value.toString()

                    val statusKategori =
                        data.child("status")
                            .value.toString()

                    if (statusKategori == "Aktif") {

                        listKategori.add(namaKategori)
                    }
                }

                btnPilihKategori.setOnClickListener {

                    AlertDialog.Builder(this)
                        .setTitle("Pilih Kategori")
                        .setItems(
                            listKategori.toTypedArray()
                        ) { _, which ->

                            kategoriDipilih =
                                listKategori[which]

                            btnPilihKategori.text =
                                kategoriDipilih
                        }
                        .show()
                }
            }
    }

    // ================= VALIDASI =================

    private fun validasi() {

        val nama =
            etNamaProduk.text.toString().trim()

        val harga =
            etHargaProduk.text.toString().trim()

        val stok =
            etStok.text.toString().trim()

        val status =
            actvStatusProduk.text.toString().trim()

        if (nama.isEmpty()) {

            etNamaProduk.error =
                "Nama produk tidak boleh kosong"

            return
        }

        if (harga.isEmpty()) {

            etHargaProduk.error =
                "Harga tidak boleh kosong"

            return
        }

        if (kategoriDipilih.isEmpty()) {

            Toast.makeText(
                this,
                "Pilih kategori dulu",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (status.isEmpty()) {

            Toast.makeText(
                this,
                "Pilih status produk",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (!cbStokTakTerbatas.isChecked &&
            stok.isEmpty()
        ) {

            etStok.error =
                "Stok tidak boleh kosong"

            return
        }

        simpanData()
    }

    // ================= SIMPAN DATA =================

    private fun simpanData() {

        val id = myRef.push().key!!

        val waktu = SimpleDateFormat(
            "dd-MM-yyyy HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        val stokProduk =
            if (cbStokTakTerbatas.isChecked)
                0
            else
                etStok.text.toString().toInt()

        val produk = ModelProduk(

            idProduk = id,

            namaProduk =
                etNamaProduk.text.toString(),

            hargaProduk =
                etHargaProduk.text.toString().toInt(),

            idKategori =
                kategoriDipilih,

            idCabang =
                cabangDipilih,

            fotoProduk =
                imageUri.toString(),

            stokProduk =
                stokProduk,

            tanpaBatas =
                cbStokTakTerbatas.isChecked,

            statusProduk =
                actvStatusProduk.text.toString(),

            createdAt = waktu,

            updatedAt = waktu
        )

        myRef.child(id)
            .setValue(produk)

            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Produk berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Gagal menyimpan data",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // ================= PERMISSION KAMERA =================

    private fun cekPermissionKamera() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            kameraLauncher.launch(null)

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                kameraLauncher.launch(null)

            } else {

                Toast.makeText(
                    this,
                    "Izin kamera ditolak",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}