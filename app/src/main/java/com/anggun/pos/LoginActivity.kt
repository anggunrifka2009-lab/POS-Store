package com.anggun.pos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Auto-login: If already logged in, go to MainActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email wajib diisi"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password wajib diisi"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        fetchUserData(uid)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Email atau password salah",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun fetchUserData(uid: String) {
        database.child("akun").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
                val editor = shared.edit()

                if (snapshot.exists()) {
                    val nama = snapshot.child("nama").value.toString()
                    val role = snapshot.child("role").value.toString()
                    editor.putString("nama", nama)
                    editor.putString("role", role)
                    editor.apply()

                    Toast.makeText(this@LoginActivity, "Selamat datang, $nama", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    val email = auth.currentUser?.email ?: "User"
                    val namaDefault = email.split("@")[0]
                    editor.putString("nama", namaDefault)
                    editor.putString("role", "Admin")
                    editor.apply()
                    
                    Toast.makeText(this@LoginActivity, "Login berhasil sebagai $namaDefault", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
