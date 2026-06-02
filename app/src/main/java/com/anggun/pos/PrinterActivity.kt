package com.anggun.pos

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections

class PrinterActivity : AppCompatActivity() {

    private lateinit var btnRefreshDevices: Button
    private lateinit var rvBluetoothDevices: RecyclerView
    private lateinit var btnTestPrint: Button
    private lateinit var btnBack: ImageView

    private lateinit var tvConnectedName: TextView
    private lateinit var tvConnectedAddress: TextView
    private lateinit var tvConnectionStatus: TextView

    private var selectedDevice: BluetoothConnection? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_printer)

        btnRefreshDevices = findViewById(R.id.btnRefreshDevices)
        rvBluetoothDevices = findViewById(R.id.rvBluetoothDevices)
        btnTestPrint = findViewById(R.id.btnTestPrint)
        btnBack = findViewById(R.id.btnBack)

        tvConnectedName = findViewById(R.id.tvConnectedDeviceName)
        tvConnectedAddress = findViewById(R.id.tvConnectedDeviceAddress)
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        rvBluetoothDevices.layoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(discoveredDevices) { device ->
            stopDiscovery()

            selectedDevice = BluetoothConnection(device)
            btnTestPrint.isEnabled = true

            @SuppressLint("MissingPermission")
            val devName = device.name ?: "Unknown Device"
            tvConnectedName.text = devName
            tvConnectedAddress.text = device.address
            tvConnectionStatus.text = "Terpilih"
            tvConnectionStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))

            val shared = getSharedPreferences("PRINTER", MODE_PRIVATE)
            shared.edit()
                .putString("name", devName)
                .putString("address", device.address)
                .apply()

            Toast.makeText(this, "Printer terpilih: $devName", Toast.LENGTH_SHORT).show()
        }
        rvBluetoothDevices.adapter = deviceAdapter

        btnBack.setOnClickListener { finish() }

        btnRefreshDevices.setOnClickListener {
            checkBluetoothAndStartDiscovery()
        }

        btnTestPrint.setOnClickListener {
            doTestPrint()
        }

        loadSavedDevice()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, filter)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && !discoveredDevices.contains(device)) {
                        discoveredDevices.add(device)
                        deviceAdapter.notifyDataSetChanged()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    btnRefreshDevices.text = "Sedang Mencari..."
                    btnRefreshDevices.isEnabled = false
                    discoveredDevices.clear()
                    deviceAdapter.notifyDataSetChanged()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    btnRefreshDevices.text = "Cari Perangkat"
                    btnRefreshDevices.isEnabled = true
                    if (discoveredDevices.isEmpty()) {
                        refreshDeviceList()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopDiscovery() {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDiscovery()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkBluetoothAndStartDiscovery() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Perangkat tidak mendukung Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (bluetoothAdapter?.isEnabled == true) {
            startDiscovery()
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetoothLauncher.launch(enableBtIntent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        if (!checkPermissions()) return

        stopDiscovery()
        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    private fun loadSavedDevice() {
        val shared = getSharedPreferences("PRINTER", MODE_PRIVATE)
        val name = shared.getString("name", "Tidak Ada")
        val address = shared.getString("address", "-")

        tvConnectedName.text = name
        tvConnectedAddress.text = address

        if (address != "-") {
            try {
                val device = bluetoothAdapter?.getRemoteDevice(address)
                if (device != null) {
                    selectedDevice = BluetoothConnection(device)
                    btnTestPrint.isEnabled = true
                    tvConnectionStatus.text = "Tersambung"
                    tvConnectionStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val requestBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startDiscovery()
        } else {
            Toast.makeText(this, "Bluetooth harus aktif untuk mencari printer", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshDeviceList() {
        if (!checkPermissions()) return

        try {
            val bluetoothDevicesList = BluetoothPrintersConnections().list

            if (bluetoothDevicesList != null && bluetoothDevicesList.isNotEmpty()) {
                discoveredDevices.clear()
                for (connection in bluetoothDevicesList) {
                    val device = connection.device
                    if (device != null && !discoveredDevices.contains(device)) {
                        discoveredDevices.add(device)
                    }
                }
                deviceAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Tidak ada printer Bluetooth paired. Hubungkan dulu di pengaturan HP!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat list printer: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 101)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startDiscovery()
            } else {
                Toast.makeText(this, "Izin Bluetooth diperlukan", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun doTestPrint() {
        val device = selectedDevice
        if (device == null) {
            Toast.makeText(this, "Pilih printer terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val printer = EscPosPrinter(device, 203, 58f, 32)
            printer.printFormattedText(
                "[C]<u><font size='big'>TEST PRINT BERHASIL</font></u>\n" +
                        "[L]\n" +
                        "[C]================================\n" +
                        "[L]Tanggal: 22-05-2026\n" +
                        "[L]Jam    : 12:00\n" +
                        "[L]\n" +
                        "[L]Item Test       1 x 5.000\n" +
                        "[L]\n" +
                        "[C]--------------------------------\n" +
                        "[L]TOTAL            Rp 5.000\n" +
                        "[C]--------------------------------\n" +
                        "[L]\n" +
                        "[C]Terima Kasih!\n" +
                        "[L]\n"
            )
            Toast.makeText(this, "Mencetak...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mencetak: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    class DeviceAdapter(private val devices: List<BluetoothDevice>, private val onClick: (BluetoothDevice) -> Unit) :
        RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvDeviceName)
            val tvAddress: TextView = view.findViewById(R.id.tvDeviceAddress)
            val btnPilih: Button = view.findViewById(R.id.btnPilih)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device_bluetooth, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = devices[position]
            holder.tvName.text = device.name ?: "Perangkat Tidak Dikenal"
            holder.tvAddress.text = device.address
            holder.btnPilih.setOnClickListener { onClick(device) }
            holder.itemView.setOnClickListener { onClick(device) }
        }

        override fun getItemCount() = devices.size
    }
}