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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import java.text.NumberFormat
import java.util.Locale

class NotaActivity : AppCompatActivity() {

    private lateinit var tvNota: TextView
    private lateinit var btnBagikan: Button
    private lateinit var btnCetak: Button
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nota)

        tvNota = findViewById(R.id.tvNota)
        btnBagikan = findViewById(R.id.btnBagikan)
        btnCetak = findViewById(R.id.btnCetak)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val alamatCabang = intent.getStringExtra("alamat_cabang") ?: "Alamat Belum Diatur"
        val kasir = intent.getStringExtra("kasir") ?: "-"
        val cabang = intent.getStringExtra("cabang") ?: "-"
        val tanggal = intent.getStringExtra("tanggal") ?: "-"
        val jam = intent.getStringExtra("jam") ?: "-"
        val items = intent.getStringExtra("items") ?: ""
        val total = intent.getIntExtra("total", 0)
        val bayar = intent.getIntExtra("bayar", 0)
        val kembali = bayar - total

        val formatter = NumberFormat.getInstance(Locale("id", "ID"))

        val nota = """
[C]<b>POS STORE</b>
[C]$alamatCabang
[C]--------------------------------
[L]Cabang  : $cabang
[L]Kasir   : $kasir
[L]Tanggal : $tanggal
[L]Jam     : $jam

--------------------------------
${items.trim()}
--------------------------------
Total   : Rp ${formatter.format(total)}
Bayar   : Rp ${formatter.format(bayar)}
Kembali : Rp ${formatter.format(kembali)}
--------------------------------
[C]Terima Kasih
[C]Silakan Datang Kembali
        """.trimIndent()


        val notaTampilan = nota
            .replace("[C]", "")
            .replace("[L]", "")
            .replace("[R]", "")
            .replace("<b>", "")
            .replace("</b>", "")

        tvNota.text = notaTampilan

        btnBagikan.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, notaTampilan)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Nota Melalui:")
            startActivity(shareIntent)
        }

        btnCetak.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
                return@setOnClickListener
            }

            val shared = getSharedPreferences("PRINTER", MODE_PRIVATE)
            val printerAddress = shared.getString("address", "-")

            if (printerAddress == "-") {
                Toast.makeText(this, "Printer belum diatur! Silakan hubungkan di pengaturan.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter
                val device = bluetoothAdapter?.getRemoteDevice(printerAddress)

                if (device != null) {
                    Toast.makeText(this, "Menghubungkan ke ${device.name ?: "Printer"}...", Toast.LENGTH_SHORT).show()

                    val connection = BluetoothConnection(device)
                    val printer = EscPosPrinter(connection, 203, 58f, 32)


                    val formatCetak = nota
                        .replace("\n", "\n[L]")
                        .replace("[C][L]", "[C]")
                        .replace("[L][L]", "[L]")
                        .replace("[R][L]", "[R]") + "\n[L]\n[L]\n"

                    printer.printFormattedText(formatCetak)
                    Toast.makeText(this, "Nota berhasil dicetak!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal menemukan sinyal printer", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal mencetak: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
