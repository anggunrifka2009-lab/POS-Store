package com.anggun.pos

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anggun.pos.cabang.DataCabangActivity
import com.anggun.pos.kategori.DataKategoriActivity
import com.anggun.pos.produk.DataProdukActivity
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val tvSapaan: TextView = findViewById(R.id.tvSapaan)
        val tvTanggal: TextView = findViewById(R.id.tvTanggal)
        val tvEstimasi: TextView = findViewById(R.id.tvEstimasi)

        val llTransaksi: LinearLayout = findViewById(R.id.llTransaksi)
        val llLaporan: LinearLayout = findViewById(R.id.llLaporan)

        val cvAkun: MaterialCardView = findViewById(R.id.cvAkun)
        val cvProduk: MaterialCardView = findViewById(R.id.cvProduk)
        val cvKategori: MaterialCardView = findViewById(R.id.cvKategori)
        val cvPegawai: MaterialCardView = findViewById(R.id.cvPegawai)
        val cvCabang: MaterialCardView = findViewById(R.id.cvCabang)
        val cvPrinter: MaterialCardView = findViewById(R.id.cvPrinter)

        setupSapaanOtomatis(tvSapaan)
        setupTanggalOtomatis(tvTanggal)
        setupEstimasiPendapatan(tvEstimasi)

        llTransaksi.setOnClickListener {
            val intent = Intent(this, TransaksiActivity::class.java)
            startActivity(intent)
        }

        llLaporan.setOnClickListener {
            showToast("Membuka Laporan")
        }

        cvAkun.setOnClickListener {
            val intent = Intent(this, AkunActivity::class.java)
            startActivity(intent)
        }

        cvProduk.setOnClickListener {
            val intent = Intent(this, DataProdukActivity::class.java)
            startActivity(intent)
        }

        cvKategori.setOnClickListener {
            val intent = Intent(this, DataKategoriActivity::class.java)
            startActivity(intent)
        }

        cvPegawai.setOnClickListener {
            val intent = Intent(this, PegawaiActivity::class.java)
            startActivity(intent)
        }

        cvCabang.setOnClickListener {
            val intent = Intent(this, DataCabangActivity::class.java)
            startActivity(intent)
        }

        cvPrinter.setOnClickListener {
            val intent = Intent(this, PrinterActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSapaanOtomatis(textView: TextView) {
        val jam = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val sapaan = when (jam) {
            in 5..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }

        val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
        val nama = shared.getString("nama", "Admin")

        textView.text = "$sapaan, $nama"
    }

    private fun setupTanggalOtomatis(textView: TextView) {
        val calendar = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat(
            "EEEE, dd MMMM yyyy",
            Locale("id", "ID")
        )
        val formattedDate = dateFormat.format(calendar)
        textView.text = formattedDate
    }

    private fun setupEstimasiPendapatan(textView: TextView) {
        val estimasi = (200000..1500000).random()
        textView.text = "Estimasi hari ini Rp ${formatRupiah(estimasi)}"
    }

    private fun formatRupiah(number: Int): String {
        return NumberFormat
            .getNumberInstance(Locale("id", "ID"))
            .format(number)
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}