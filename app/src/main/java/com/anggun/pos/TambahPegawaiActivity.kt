package com.anggun.pos

import android.os.Bundle
import android.view.View
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
import com.anggun.pos.model.ModelPegawai
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TambahPegawaiActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etFoto: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tlPassword: TextInputLayout
    private lateinit var actvRole: AutoCompleteTextView
    private lateinit var btnSimpan: Button
    private lateinit var tvJudul: TextView
    private lateinit var ivKembali: ImageView

    private var idPegawaiTerpilih: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pegawai)

        init()
        setupRoleDropdown()

        ivKembali.setOnClickListener {
            finish()
        }

        @Suppress("DEPRECATION")
        val dataIntent = intent.getParcelableExtra<ModelPegawai>("DATA_PEGAWAI")
        if (dataIntent != null) {
            setupModeEdit(dataIntent)
        }

        btnSimpan.setOnClickListener {
            cekValidasi()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun init() {
        etNama = findViewById(R.id.etNamaPegawai)
        etAlamat = findViewById(R.id.etAlamatPegawai)
        etFoto = findViewById(R.id.etFotoPegawai)
        etEmail = findViewById(R.id.etEmailPegawai)
        etPassword = findViewById(R.id.etPasswordPegawai)
        tlPassword = findViewById(R.id.tlPasswordPegawai)
        actvRole = findViewById(R.id.actvRolePegawai)
        btnSimpan = findViewById(R.id.btnSimpanPegawai)
        tvJudul = findViewById(R.id.tvJudul)
        ivKembali = findViewById(R.id.ivKembali)
    }

    private fun setupRoleDropdown() {
        val roles = arrayOf("Kasir", "Manajer")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roles)
        actvRole.setAdapter(adapter)
    }

    private fun setupModeEdit(pegawai: ModelPegawai) {
        idPegawaiTerpilih = pegawai.idPegawai
        etNama.setText(pegawai.nama)
        etAlamat.setText(pegawai.alamat)
        etFoto.setText(pegawai.foto)
        actvRole.setText(pegawai.role, false)
        
        tlPassword.visibility = View.GONE
        etEmail.isEnabled = false
        
        tvJudul.text = "Ubah Pegawai"
        btnSimpan.text = "Perbarui"

        val db = FirebaseDatabase.getInstance().reference
        db.child("akun").child(pegawai.idPegawai!!).child("email").get().addOnSuccessListener {
            etEmail.setText(it.value?.toString() ?: "")
        }
    }

    private fun cekValidasi() {
        val nama = etNama.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val foto = etFoto.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val pass = etPassword.text.toString().trim()
        val role = actvRole.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = "Nama wajib diisi"
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email wajib diisi"
            return
        }

        if (idPegawaiTerpilih == null && pass.isEmpty()) {
            etPassword.error = "Password wajib diisi"
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

        if (idPegawaiTerpilih == null) {
            registerDanSimpan(nama, email, pass, role, alamat, foto)
        } else {
            simpan(idPegawaiTerpilih!!, nama, alamat, role, foto)
        }
    }

    private fun registerDanSimpan(nama: String, email: String, pass: String, role: String, alamat: String, foto: String) {
        val options = FirebaseOptions.Builder()
            .setApiKey("AIzaSyApZ1TvNN-zD_XzKUwMEY6zcRH-hdxiPYo")
            .setApplicationId("1:105374761032:android:e91b074ec22c170e3a187d")
            .setProjectId("posject")
            .setDatabaseUrl("https://posject-default-rtdb.firebaseio.com")
            .build()

        val secondaryApp = try {
            FirebaseApp.getInstance("Secondary")
        } catch (e: Exception) {
            FirebaseApp.initializeApp(this, options, "Secondary")
        }

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        btnSimpan.isEnabled = false
        btnSimpan.text = "Memproses..."

        secondaryAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    val userMap = HashMap<String, Any>()
                    userMap["uid"] = uid
                    userMap["nama"] = nama
                    userMap["email"] = email
                    userMap["role"] = role
                    userMap["foto"] = foto

                    val db = FirebaseDatabase.getInstance().reference
                    db.child("akun").child(uid).setValue(userMap)
                        .addOnSuccessListener {
                            simpan(uid, nama, alamat, role, foto)
                        }
                }
                secondaryAuth.signOut()
            }
            .addOnFailureListener {
                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
                Toast.makeText(this, "Gagal Registrasi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun simpan(id: String, nama: String, alamat: String, role: String, foto: String) {
        val db = FirebaseDatabase.getInstance().reference

        val pegawai = ModelPegawai(
            idPegawai = id,
            nama = nama,
            alamat = alamat,
            role = role,
            foto = foto
        )

        db.child("pegawai").child(id).setValue(pegawai)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil simpan pegawai", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
                Toast.makeText(this, "Gagal simpan database: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
