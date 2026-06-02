package com.anggun.pos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.adapter.TransaksiAdapter
import com.anggun.pos.model.ModelCabang
import com.anggun.pos.model.ModelTransaksi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransaksiActivity : AppCompatActivity() {

    private lateinit var rvProduk: RecyclerView
    private lateinit var etBayar: EditText
    private lateinit var etCariProduk: EditText
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var spCabang: AutoCompleteTextView
    private lateinit var spKategori: AutoCompleteTextView
    private lateinit var ivKembali: ImageView

    private lateinit var database: DatabaseReference

    private var totalBelanja = 0
    private var namaKasir = ""
    private var selectedCabang = ""
    private var selectedKategori = "Semua Kategori"
    private var selectedAlamatCabang = ""

    private val listProduk = ArrayList<ModelTransaksi>()
    private val listProdukFull = ArrayList<ModelTransaksi>()
    private val listCabang = ArrayList<String>()
    private val listKategori = ArrayList<String>()
    private val listModelCabang = ArrayList<ModelCabang>()

    private lateinit var adapter: TransaksiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        rvProduk = findViewById(R.id.rvProduk)
        etBayar = findViewById(R.id.etBayar)
        etCariProduk = findViewById(R.id.etCariProduk)
        tvTotal = findViewById(R.id.tvTotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        spCabang = findViewById(R.id.spCabang)
        spKategori = findViewById(R.id.spKategori)
        ivKembali = findViewById(R.id.ivKembali)

        database = FirebaseDatabase.getInstance().reference

        ivKembali.setOnClickListener {
            finish()
        }

        val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
        namaKasir = shared.getString("nama", "").toString()

        if (namaKasir.isEmpty()) {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user != null) {
                database.child("akun").child(user.uid).child("nama")
                    .get().addOnSuccessListener {
                        namaKasir = it.value?.toString() ?: user.email?.split("@")?.get(0) ?: "Admin"
                        shared.edit().putString("nama", namaKasir).apply()
                    }
            } else {
                namaKasir = "Admin"
            }
        }

        rvProduk.layoutManager = LinearLayoutManager(this)

        adapter = TransaksiAdapter(
            listProduk,
            object : TransaksiAdapter.OnQtyChange {
                override fun onChanged(total: Int) {
                    calculateTotal()
                }
            }
        )

        rvProduk.adapter = adapter

        loadProduk()
        loadCabang()
        loadKategori()

        etCariProduk.addTextChangedListener { s ->
            filterProduk(s.toString())
        }

        btnCheckout.setOnClickListener {

            if (totalBelanja == 0) {
                Toast.makeText(this, "Pilih produk dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedCabang.isEmpty()) {
                Toast.makeText(this, "Silakan pilih cabang terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bayarText = etBayar.text.toString().trim()

            if (bayarText.isEmpty()) {
                etBayar.error = "Masukkan pembayaran"
                etBayar.requestFocus()
                return@setOnClickListener
            }

            val bayar = bayarText.toInt()

            if (bayar < totalBelanja) {
                Toast.makeText(this, "Uang kurang", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            simpanTransaksi(bayar)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadCabang() {
        database.child("cabang").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                listModelCabang.clear()
                for (data in snapshot.children) {
                    val cabang = data.getValue(ModelCabang::class.java)
                    if (cabang != null && cabang.status == "Aktif") {
                        listModelCabang.add(cabang)
                        listCabang.add(cabang.namaCabang ?: "Unknown")
                    }
                }
                val adapterCabang = ArrayAdapter(this@TransaksiActivity, android.R.layout.simple_list_item_1, listCabang)
                spCabang.setAdapter(adapterCabang)

                // Jika cabang yang sedang terpilih tiba-tiba tidak aktif
                if (!listCabang.contains(selectedCabang)) {
                    selectedCabang = ""
                    selectedAlamatCabang = ""
                    spCabang.setText("", false)
                    filterProduk(etCariProduk.text.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal memuat cabang", Toast.LENGTH_SHORT).show()
            }
        })

        spCabang.setOnItemClickListener { parent, view, position, id ->
            selectedCabang = listCabang[position]
            val modelTerpilih = listModelCabang[position]
            selectedAlamatCabang = modelTerpilih.alamatCabang ?: "Alamat tidak diatur"
            filterProduk(etCariProduk.text.toString())
        }
    }

    private fun loadKategori() {
        database.child("kategori").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategori.clear()
                listKategori.add("Semua Kategori")
                for (data in snapshot.children) {
                    val nama = data.child("namaKategori").value?.toString()
                    val status = data.child("status").value?.toString()
                    if (nama != null && status == "Aktif") {
                        listKategori.add(nama)
                    }
                }
                val adapterKategori = ArrayAdapter(this@TransaksiActivity, android.R.layout.simple_list_item_1, listKategori)
                spKategori.setAdapter(adapterKategori)
                
                if (!listKategori.contains(selectedKategori)) {
                    selectedKategori = "Semua Kategori"
                    spKategori.setText("Semua Kategori", false)
                }
                
                filterProduk(etCariProduk.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransaksiActivity, "Gagal memuat kategori", Toast.LENGTH_SHORT).show()
            }
        })

        spKategori.setOnItemClickListener { _, _, position, _ ->
            selectedKategori = listKategori[position]
            filterProduk(etCariProduk.text.toString())
        }
    }

    private fun loadProduk() {
        database.child("Produk")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentQty = listProdukFull.associate { (it.idProduk ?: "") to it.qty }

                    listProdukFull.clear()
                    for (data in snapshot.children) {
                        val produk = data.getValue(ModelTransaksi::class.java)
                        if (produk != null && produk.idProduk != null) {
                            produk.qty = currentQty[produk.idProduk] ?: 0
                            listProdukFull.add(produk)
                        }
                    }
                    filterProduk(etCariProduk.text.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TransaksiActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterProduk(query: String) {
        if (selectedCabang.isEmpty()) {
            listProduk.clear()
            adapter.notifyDataSetChanged()
            return
        }

        val filteredList = listProdukFull.filter {
            val matchesQuery = it.namaProduk?.lowercase()?.contains(query.lowercase()) == true
            val matchesCabang = it.idCabang == selectedCabang || it.idCabang == "Semua Cabang"
            val matchesStatus = it.statusProduk == "Aktif"
            
            // Produk harus memiliki kategori yang sedang aktif
            val isKategoriAktif = listKategori.contains(it.idKategori)
            
            val matchesKategori = if (selectedKategori == "Semua Kategori") {
                isKategoriAktif
            } else {
                it.idKategori == selectedKategori
            }
            
            matchesQuery && matchesCabang && matchesStatus && matchesKategori
        }
        listProduk.clear()
        listProduk.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    private fun calculateTotal() {
        totalBelanja = listProdukFull.sumOf { (it.hargaProduk ?: 0) * it.qty }
        tvTotal.text = "Rp. $totalBelanja"
    }

    private fun simpanTransaksi(bayar: Int) {
        val idTransaksi = database.child("transaksi").push().key ?: return

        val tanggal = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val jam = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val mapTransaksi = HashMap<String, Any>()
        mapTransaksi["idTransaksi"] = idTransaksi
        mapTransaksi["kasir"] = namaKasir
        mapTransaksi["cabang"] = selectedCabang
        mapTransaksi["alamat_cabang"] = selectedAlamatCabang
        mapTransaksi["tanggal"] = tanggal
        mapTransaksi["jam"] = jam
        mapTransaksi["total"] = totalBelanja
        mapTransaksi["bayar"] = bayar

        database.child("transaksi")
            .child(idTransaksi)
            .setValue(mapTransaksi)
            .addOnSuccessListener {
                simpanItemTransaksi(idTransaksi, tanggal, jam, bayar)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal simpan transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun simpanItemTransaksi(
        idTransaksi: String,
        tanggal: String,
        jam: String,
        bayar: Int
    ) {
        val listItems = StringBuilder()
        for (produk in listProdukFull) {
            if (produk.qty > 0) {
                val subtotal = (produk.hargaProduk ?: 0) * produk.qty
                listItems.append("${produk.namaProduk}\n")
                listItems.append("${produk.qty} x Rp ${produk.hargaProduk} = Rp $subtotal\n")

                val mapItem = HashMap<String, Any>()
                mapItem["idProduk"] = produk.idProduk ?: ""
                mapItem["namaProduk"] = produk.namaProduk ?: ""
                mapItem["harga"] = produk.hargaProduk ?: 0
                mapItem["qty"] = produk.qty
                mapItem["subtotal"] = subtotal

                database.child("transaksi")
                    .child(idTransaksi)
                    .child("items")
                    .push()
                    .setValue(mapItem)

                if (produk.tanpaBatas != true && produk.idProduk != null) {
                    val sisaStok = (produk.stokProduk ?: 0) - produk.qty
                    database.child("Produk").child(produk.idProduk!!).child("stokProduk").setValue(sisaStok)
                }
            }
        }

        Toast.makeText(this, "Transaksi berhasil", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, NotaActivity::class.java)
        intent.putExtra("kasir", namaKasir)
        intent.putExtra("cabang", selectedCabang)
        intent.putExtra("alamat_cabang", selectedAlamatCabang)
        intent.putExtra("tanggal", tanggal)
        intent.putExtra("jam", jam)
        intent.putExtra("total", totalBelanja)
        intent.putExtra("bayar", bayar)
        intent.putExtra("items", listItems.toString())

        startActivity(intent)
        finish()
    }
}