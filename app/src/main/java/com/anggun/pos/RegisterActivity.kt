package com.anggun.pos

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        etNama = findViewById(R.id.etNama)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)

        tvLogin.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nama.isEmpty()) {
                etNama.error = "Nama wajib diisi"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Email wajib diisi"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password wajib diisi"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password minimal 6 karakter"
                return@setOnClickListener
            }

            registerUser(nama, email, password)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun registerUser(nama: String, email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    val userMap = HashMap<String, Any>()
                    userMap["uid"] = uid
                    userMap["nama"] = nama
                    userMap["email"] = email
                    userMap["role"] = "Admin" // Default role

                    database.child("akun").child(uid).setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal simpan data: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Registrasi Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
