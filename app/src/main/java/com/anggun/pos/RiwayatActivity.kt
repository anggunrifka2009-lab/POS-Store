package com.anggun.pos

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
        adapterRiwayat = RiwayatAdapter(listRiwayat)
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
}
