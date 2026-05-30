package com.anggun.pos

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import java.text.NumberFormat
import java.util.Locale

class NotaActivity : AppCompatActivity() {

    private lateinit var tvAlamatNota: TextView
    private lateinit var tvNota: TextView
    private lateinit var btnBagikan: Button
    private lateinit var btnCetak: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nota)

        // Inisialisasi View ID dari XML
        tvAlamatNota = findViewById(R.id.tvAlamatNota)
        tvNota = findViewById(R.id.tvNota)
        btnBagikan = findViewById(R.id.btnBagikan)
        btnCetak = findViewById(R.id.btnCetak)
        toolbar = findViewById(R.id.toolbar)

        // Setup Toolbar dan Tombol Kembali
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Tangkap data kiriman dari TransaksiActivity
        val alamatCabang = intent.getStringExtra("alamat_cabang") ?: "Alamat Belum Diatur"
        val kasir = intent.getStringExtra("kasir") ?: "-"
        val cabang = intent.getStringExtra("cabang") ?: "-"
        val tanggal = intent.getStringExtra("tanggal") ?: "-"
        val jam = intent.getStringExtra("jam") ?: "-"
        val items = intent.getStringExtra("items") ?: ""
        val total = intent.getIntExtra("total", 0)
        val bayar = intent.getIntExtra("bayar", 0)
        val kembali = bayar - total

        tvAlamatNota.text = alamatCabang

        val formatter = NumberFormat.getInstance(Locale("id", "ID"))

        // Format tampilan teks nota monoton di layar HP
        val nota = """
Cabang  : $cabang
Kasir   : $kasir
Tanggal : $tanggal
Jam     : $jam

--------------------------------
${items.trim()}
--------------------------------
Total   : Rp ${formatter.format(total)}
Bayar   : Rp ${formatter.format(bayar)}
Kembali : Rp ${formatter.format(kembali)}
--------------------------------
        """.trimIndent()

        tvNota.text = nota

        // Aksi tombol bagikan teks nota ke aplikasi lain (WA, dll)
        btnBagikan.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, nota)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Nota Melalui:")
            startActivity(shareIntent)
        }

        // Aksi tombol cetak langsung menggunakan data printer dari PrinterActivity
        btnCetak.setOnClickListener {
            // 1. Cek Permission koneksi Bluetooth (Android 12+)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
                return@setOnClickListener
            }

            // 2. Ambil data alamat MAC Printer dari SharedPreferences halaman pengaturan printer
            val shared = getSharedPreferences("PRINTER", MODE_PRIVATE)
            val printerAddress = shared.getString("address", "-")

            if (printerAddress == "-") {
                Toast.makeText(this, "Printer belum diatur! Silakan hubungkan di pengaturan.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                // 3. Cari Perangkat Bluetooth berdasarkan MAC address
                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter
                val device = bluetoothAdapter?.getRemoteDevice(printerAddress)

                if (device != null) {
                    Toast.makeText(this, "Menghubungkan ke ${device.name ?: "Printer"}...", Toast.LENGTH_SHORT).show()

                    val connection = BluetoothConnection(device)
                    // Menggunakan setting ukuran 58f standar (32 karakter per baris)
                    val printer = EscPosPrinter(connection, 203, 58f, 32)

                    // Format cetak thermal terstruktur rapi rata kiri-kanan
                    // Menggunakan [C] untuk Center, [L] untuk Left, [R] untuk Right
                    val formatCetak = """
                        [C]<b>POS STORE</b>
                        [C]$alamatCabang
                        [C]--------------------------------
                        [L]Cabang  : $cabang
                        [L]Kasir   : $kasir
                        [L]Tanggal : $tanggal
                        [L]Jam     : $jam
                        [C]--------------------------------
                        [L]${items.trim().replace("\n", "\n[L]")}
                        [C]--------------------------------
                        [L]Total   : [R]Rp ${formatter.format(total)}
                        [L]Bayar   : [R]Rp ${formatter.format(bayar)}
                        [L]Kembali : [R]Rp ${formatter.format(kembali)}
                        [C]--------------------------------
                        [C]Terima Kasih
                        [C]Silakan Datang Kembali
                        [L]\n
                        [L]\n
                    """.trimIndent()

                    printer.printFormattedText(formatCetak)
                    Toast.makeText(this, "Nota berhasil dicetak!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal menemukan sinyal printer", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal mencetak: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
