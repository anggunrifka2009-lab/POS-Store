package com.anggun.pos

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anggun.pos.adapter.RiwayatAdapter
import com.anggun.pos.model.ModelRiwayat
import com.google.firebase.database.*

class RiwayatActivity : AppCompatActivity() {

    private lateinit var rvRiwayat: RecyclerView
    private lateinit var ivKembali: ImageView
    private lateinit var adapterRiwayat: RiwayatAdapter
    private val listRiwayat = ArrayList<ModelRiwayat>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_riwayat)

        database = FirebaseDatabase.getInstance().reference

        rvRiwayat = findViewById(R.id.rvRiwayat)
        ivKembali = findViewById(R.id.ivKembali)

        ivKembali.setOnClickListener { finish() }

        rvRiwayat.layoutManager = LinearLayoutManager(this)
        adapterRiwayat = RiwayatAdapter(listRiwayat) { riwayat ->
            showNota(riwayat)
        }
        rvRiwayat.adapter = adapterRiwayat

        fetchRiwayatTransaksi()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchRiwayatTransaksi() {
        database.child("transaksi").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listRiwayat.clear()
                for (data in snapshot.children) {
                    val riwayat = data.getValue(ModelRiwayat::class.java)
                    if (riwayat != null) {
                        listRiwayat.add(riwayat)
                    }
                }
                listRiwayat.reverse() // Terbaru di atas
                adapterRiwayat.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RiwayatActivity, "Gagal memuat riwayat", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showNota(riwayat: ModelRiwayat) {
        val idTransaksi = riwayat.idTransaksi ?: return
        
        // Ambil data items untuk nota
        database.child("transaksi").child(idTransaksi).child("items")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listItems = StringBuilder()
                    for (item in snapshot.children) {
                        val namaProduk = item.child("namaProduk").value?.toString() ?: ""
                        val qty = item.child("qty").value?.toString()?.toIntOrNull() ?: 0
                        val harga = item.child("harga").value?.toString()?.toIntOrNull() ?: 0
                        val subtotal = item.child("subtotal").value?.toString()?.toIntOrNull() ?: 0
                        
                        listItems.append("$namaProduk\n")
                        listItems.append("$qty x Rp $harga = Rp $subtotal\n")
                    }

                    val intent = Intent(this@RiwayatActivity, NotaActivity::class.java)
                    intent.putExtra("kasir", riwayat.kasir)
                    intent.putExtra("cabang", riwayat.cabang)
                    intent.putExtra("alamat_cabang", riwayat.alamat_cabang)
                    intent.putExtra("tanggal", riwayat.tanggal)
                    intent.putExtra("jam", riwayat.jam)
                    intent.putExtra("total", riwayat.total)
                    intent.putExtra("bayar", riwayat.bayar)
                    intent.putExtra("items", listItems.toString())
                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RiwayatActivity, "Gagal memuat detail nota", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
