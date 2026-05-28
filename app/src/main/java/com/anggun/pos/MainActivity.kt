package com.anggun.pos

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anggun.pos.cabang.DataCabangActivity
import com.anggun.pos.kategori.DataKategoriActivity
import com.anggun.pos.produk.DataProdukActivity
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            showToast("Menu Transaksi")
        }

        llLaporan.setOnClickListener {
            showToast("Membuka Laporan")
        }

        cvAkun.setOnClickListener {
            showToast("Menu Akun")
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
            showToast("Menu Pegawai")
        }

        cvCabang.setOnClickListener {
            val intent = Intent(this, com.anggun.pos.cabang.DataCabangActivity::class.java)
            startActivity(intent)
        }

        cvPrinter.setOnClickListener {
            showToast("Menu Printer")
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

        textView.text = "$sapaan, Anggun"
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

        textView.text =
            "Estimasi hari ini Rp ${formatRupiah(estimasi)}"
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