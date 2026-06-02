package com.anggun.pos

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var ivKembali: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nota)

        tvNota = findViewById(R.id.tvNota)
        btnBagikan = findViewById(R.id.btnBagikan)
        btnCetak = findViewById(R.id.btnCetak)
        ivKembali = findViewById<ImageView>(R.id.ivKembali)

        ivKembali.setOnClickListener {
            finish()
        }

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
[C]<b>${cabang.uppercase()}</b>
[C]$alamatCabang
[C]--------------------------------
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

        val notaTampilanHtml = nota
            .split("\n")
            .joinToString("<br>") { line ->
                if (line.contains("[C]")) {
                    val cleanText = line.replace("[C]", "").replace("<b>", "").replace("</b>", "")
                    val paddingSize = (32 - cleanText.length) / 2
                    val spaces = if (paddingSize > 0) "&nbsp;".repeat(paddingSize) else ""
                    val content = line.replace("[C]", "")
                    spaces + content
                } else {
                    line.replace("[L]", "").replace("[R]", "")
                }
            }

        tvNota.text = android.text.Html.fromHtml(notaTampilanHtml, android.text.Html.FROM_HTML_MODE_LEGACY)

        btnBagikan.setOnClickListener {
            val notaPolos = nota
                .replace("[C]", "")
                .replace("[L]", "")
                .replace("[R]", "")
                .replace("<b>", "")
                .replace("</b>", "")
            
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, notaPolos)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Nota Melalui:")
            startActivity(shareIntent)
        }

        btnCetak.setOnClickListener {
            val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
            } else {
                arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
            }

            val missingPermissions = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 1)
                return@setOnClickListener
            }

            val shared = getSharedPreferences("PRINTER", MODE_PRIVATE)
            val printerAddress = shared.getString("address", "-")

            if (printerAddress == "-" || printerAddress.isNullOrEmpty()) {
                Toast.makeText(this, "Printer belum diatur! Silakan hubungkan di pengaturan.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter
                
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    Toast.makeText(this, "Bluetooth tidak aktif!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val device = bluetoothAdapter.getRemoteDevice(printerAddress)

                if (device != null) {
                    Toast.makeText(this, "Menghubungkan ke printer...", Toast.LENGTH_SHORT).show()
                    
                    val connection = BluetoothConnection(device)
                    val printer = EscPosPrinter(connection, 203, 58f, 32)

                    val sb = StringBuilder()
                    sb.append("[C]<b>${cabang.uppercase()}</b>\n")
                    sb.append("[C]$alamatCabang\n")
                    sb.append("[C]--------------------------------\n")
                    sb.append("[L]Kasir   : $kasir\n")
                    sb.append("[L]Tanggal : $tanggal\n")
                    sb.append("[L]Jam     : $jam\n")
                    sb.append("[L]\n")
                    sb.append("[C]--------------------------------\n")
                    
                    items.trim().split("\n").forEach { line ->
                        if (line.isNotEmpty()) {
                            sb.append("[L]$line\n")
                        }
                    }
                    
                    sb.append("[C]--------------------------------\n")
                    sb.append("[L]Total   : Rp ${formatter.format(total)}\n")
                    sb.append("[L]Bayar   : Rp ${formatter.format(bayar)}\n")
                    sb.append("[L]Kembali : Rp ${formatter.format(kembali)}\n")
                    sb.append("[C]--------------------------------\n")
                    sb.append("[C]Terima Kasih\n")
                    sb.append("[C]Silakan Datang Kembali\n")
                    sb.append("\n\n\n")

                    printer.printFormattedText(sb.toString())
                    Toast.makeText(this, "Nota berhasil dicetak!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal menemukan printer", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
