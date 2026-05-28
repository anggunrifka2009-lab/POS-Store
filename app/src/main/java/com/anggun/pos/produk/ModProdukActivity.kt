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
import android.widget.TextView
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
    private var tvJudul: TextView? = null

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("Produk")

    private var kategoriDipilih = ""
    private var cabangDipilih = ""

    private var idProdukTerpilih: String? = null
    private var dataProdukOld: ModelProduk? = null

    private var fotoStringTerpilih: String = ""
    private var bitmapFoto: Bitmap? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    private val galeriLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                bitmapFoto = null
                fotoStringTerpilih = uri.toString()

                // PERBAIKAN UTAMA: Ambil izin akses baca permanen untuk URI Galeri
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                imgPreview.imageTintList = null
                imgPreview.setImageURI(uri)
            }
        }
    }

    private val kameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            bitmapFoto = bitmap
            fotoStringTerpilih = ""
            imgPreview.imageTintList = null
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
        ambilCabang()

        ivKembali.setOnClickListener { finish() }
        btnKamera.setOnClickListener { cekPermissionKamera() }

        btnGaleri.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            galeriLauncher.launch(intent)
        }

        val dataIntent = intent.getParcelableExtra<ModelProduk>("DATA_PRODUK")
        if (dataIntent != null) {
            setupModeEdit(dataIntent)
        }

        btnSimpan.setOnClickListener { validasi() }
    }

    private fun init() {
        ivKembali = findViewById(R.id.ivKembali)
        btnKamera = findViewById(R.id.btnKamera)
        btnGaleri = findViewById(R.id.btnGaleri)
        imgPreview = findViewById(R.id.imgProduk)
        etNamaProduk = findViewById(R.id.etNamaProduk)
        etHargaProduk = findViewById(R.id.etHargaProduk)
        etStok = findViewById(R.id.etStok)
        actvStatusProduk = findViewById(R.id.actvStatusProduk)
        cbStokTakTerbatas = findViewById(R.id.cbStokTakTerbatas)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnPilihKategori = findViewById(R.id.btnPilihKategori)
        btnPilihCabang = findViewById(R.id.btnPilihCabang)
        tvJudul = findViewById(R.id.tvJudul)
    }

    private fun setupDropdownStatus() {
        val statusArray = arrayOf("Aktif", "Nonaktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusArray)
        actvStatusProduk.setAdapter(adapter)
    }

    private fun setupModeEdit(produk: ModelProduk) {
        dataProdukOld = produk
        idProdukTerpilih = produk.idProduk

        etNamaProduk.setText(produk.namaProduk)
        etHargaProduk.setText(produk.hargaProduk.toString())

        cbStokTakTerbatas.isChecked = produk.tanpaBatas == true
        if (produk.tanpaBatas == true) {
            etStok.setText("")
        } else {
            etStok.setText(produk.stokProduk.toString())
        }

        actvStatusProduk.setText(produk.statusProduk, false)

        kategoriDipilih = produk.idKategori ?: ""
        btnPilihKategori.text = if (kategoriDipilih.isEmpty()) "Pilih Kategori" else kategoriDipilih

        cabangDipilih = produk.idCabang ?: ""
        btnPilihCabang.text = if (cabangDipilih.isEmpty()) "Pilih Cabang" else cabangDipilih

        if (!produk.fotoProduk.isNullOrEmpty() && produk.fotoProduk != "bitmap_kamera_saved") {
            fotoStringTerpilih = produk.fotoProduk
            try {
                imgPreview.imageTintList = null
                imgPreview.setImageURI(Uri.parse(produk.fotoProduk))
            } catch (e: Exception) {
                imgPreview.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }

        tvJudul?.text = "Ubah Produk"
        btnSimpan.text = "Perbarui"
    }

    private fun ambilKategori() {
        val kategoriRef = FirebaseDatabase.getInstance().getReference("kategori")
        kategoriRef.get().addOnSuccessListener { snapshot ->
            val listKategori = ArrayList<String>()
            for (data in snapshot.children) {
                val namaKategori = data.child("namaKategori").value.toString()
                val statusKategori = data.child("status").value.toString()
                if (statusKategori == "Aktif") {
                    listKategori.add(namaKategori)
                }
            }

            btnPilihKategori.setOnClickListener {
                if (listKategori.isEmpty()) {
                    Toast.makeText(this, "Belum ada kategori aktif", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                AlertDialog.Builder(this)
                    .setTitle("Pilih Kategori")
                    .setItems(listKategori.toTypedArray()) { _, which ->
                        kategoriDipilih = listKategori[which]
                        btnPilihKategori.text = kategoriDipilih
                    }
                    .show()
            }
        }
    }

    private fun ambilCabang() {
        val cabangRef = FirebaseDatabase.getInstance().getReference("cabang")
        cabangRef.get().addOnSuccessListener { snapshot ->
            val listCabang = ArrayList<String>()
            for (data in snapshot.children) {
                val namaCabang = data.child("namaCabang").value?.toString()
                    ?: data.child("nama").value?.toString()
                    ?: data.child("nama_cabang").value?.toString()

                if (!namaCabang.isNullOrEmpty()) {
                    listCabang.add(namaCabang)
                }
            }

            btnPilihCabang.setOnClickListener {
                if (listCabang.isEmpty()) {
                    Toast.makeText(this, "Belum ada data cabang", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                AlertDialog.Builder(this)
                    .setTitle("Pilih Cabang")
                    .setItems(listCabang.toTypedArray()) { _, which ->
                        cabangDipilih = listCabang[which]
                        btnPilihCabang.text = cabangDipilih
                    }
                    .show()
            }
        }
    }

    private fun validasi() {
        val nama = etNamaProduk.text.toString().trim()
        val harga = etHargaProduk.text.toString().trim()
        val stok = etStok.text.toString().trim()
        val status = actvStatusProduk.text.toString().trim()

        if (nama.isEmpty()) { etNamaProduk.error = "Nama produk tidak boleh kosong"; return }
        if (harga.isEmpty()) { etHargaProduk.error = "Harga tidak boleh kosong"; return }
        if (kategoriDipilih.isEmpty()) { Toast.makeText(this, "Pilih kategori dulu", Toast.LENGTH_SHORT).show(); return }
        if (cabangDipilih.isEmpty()) { Toast.makeText(this, "Pilih cabang dulu", Toast.LENGTH_SHORT).show(); return }
        if (status.isEmpty()) { Toast.makeText(this, "Pilih status produk", Toast.LENGTH_SHORT).show(); return }
        if (cbStokTakTerbatas.isChecked == false && stok.isEmpty()) { etStok.error = "Stok tidak boleh kosong"; return }

        simpanData(nama, harga, stok, status)
    }

    private fun simpanData(nama: String, harga: String, stok: String, status: String) {
        val id = idProdukTerpilih ?: myRef.push().key!!

        val waktuSekarang = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val waktuBuat = dataProdukOld?.createdAt ?: waktuSekarang

        val stokProduk = if (cbStokTakTerbatas.isChecked) 0 else stok.toInt()

        val finalFotoString = if (bitmapFoto != null) {
            "bitmap_kamera_saved"
        } else if (fotoStringTerpilih.isNotEmpty()) {
            fotoStringTerpilih
        } else {
            dataProdukOld?.fotoProduk ?: ""
        }

        val produk = ModelProduk(
            idProduk = id,
            namaProduk = nama,
            hargaProduk = harga.toInt(),
            idKategori = kategoriDipilih,
            idCabang = cabangDipilih,
            fotoProduk = finalFotoString,
            stokProduk = stokProduk,
            tanpaBatas = cbStokTakTerbatas.isChecked,
            statusProduk = status,
            createdAt = waktuBuat,
            updatedAt = waktuSekarang
        )

        myRef.child(id).setValue(produk)
            .addOnSuccessListener {
                val pesan = if (idProdukTerpilih == null) "Produk berhasil disimpan" else "Produk berhasil diperbarui"
                Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cekPermissionKamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            kameraLauncher.launch(null)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            kameraLauncher.launch(null)
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }
}