package com.anggun.pos

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anggun.pos.model.ModelPegawai
import com.google.firebase.database.FirebaseDatabase

class TambahPegawaiActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etFoto: EditText
    private lateinit var actvRole: AutoCompleteTextView
    private lateinit var btnSimpan: Button
    private lateinit var tvJudul: TextView
    private lateinit var ivKembali: ImageView

    private var idPegawaiTerpilih: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pegawai)

        init()
        setupRoleDropdown()

        ivKembali.setOnClickListener {
            finish()
        }

        val dataIntent = intent.getParcelableExtra<ModelPegawai>("DATA_PEGAWAI")
        if (dataIntent != null) {
            setupModeEdit(dataIntent)
        }

        btnSimpan.setOnClickListener {
            cekValidasi()
        }
    }

    private fun init() {
        etNama = findViewById(R.id.etNamaPegawai)
        etAlamat = findViewById(R.id.etAlamatPegawai)
        etFoto = findViewById(R.id.etFotoPegawai)
        actvRole = findViewById(R.id.actvRolePegawai)
        btnSimpan = findViewById(R.id.btnSimpanPegawai)
        tvJudul = findViewById(R.id.tvJudul)
        ivKembali = findViewById(R.id.ivKembali)
    }

    private fun setupRoleDropdown() {
        val roles = arrayOf("Admin", "Kasir", "Manajer")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roles)
        actvRole.setAdapter(adapter)
    }

    private fun setupModeEdit(pegawai: ModelPegawai) {
        idPegawaiTerpilih = pegawai.idPegawai
        etNama.setText(pegawai.nama)
        etAlamat.setText(pegawai.alamat)
        etFoto.setText(pegawai.foto)
        actvRole.setText(pegawai.role, false)
        tvJudul.text = "Ubah Pegawai"
        btnSimpan.text = "Perbarui"
    }

    private fun cekValidasi() {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val foto = etFoto.text.toString().trim()
        val role = actvRole.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama wajib diisi"
            return
        }

        if (alamat.isEmpty()) {
            etAlamat.error = "Alamat wajib diisi"
            return
        }

        if (role.isEmpty()) {
            actvRole.error = "Role wajib dipilih"
            return
        }

        simpan(nama, alamat, role, foto)
    }

    private fun simpan(nama: String, alamat: String, role: String, foto: String) {
        val db = FirebaseDatabase.getInstance().reference
        val id = idPegawaiTerpilih ?: db.child("pegawai").push().key ?: return

        val pegawai = ModelPegawai(
            idPegawai = id,
            nama = nama,
            alamat = alamat,
            role = role,
            foto = foto
        )

        db.child("pegawai").child(id).setValue(pegawai)
            .addOnSuccessListener {
                val pesan = if (idPegawaiTerpilih == null) "Berhasil simpan" else "Berhasil update"
                Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
