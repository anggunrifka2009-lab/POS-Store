package com.anggun.pos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AkunActivity : AppCompatActivity() {

    private lateinit var tvNama: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var btnLogout: Button

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)

        database = FirebaseDatabase.getInstance().reference

        tvNama = findViewById(R.id.tvNamaProfile)
        tvEmail = findViewById(R.id.tvEmailProfile)
        tvRole = findViewById(R.id.tvRoleProfile)
        btnLogout = findViewById(R.id.btnLogout)
        
        findViewById<android.widget.ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        loadProfile()

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
            shared.edit().clear().apply()
            
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadProfile() {
        val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
        val nama = shared.getString("nama", null)
        val role = shared.getString("role", null)
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "-"

        if (nama != null && role != null) {
            tvNama.text = nama
            tvEmail.text = email
            tvRole.text = role
        } else if (user != null) {
            // Jika SharedPreferences kosong, ambil dari database
            database.child("akun").child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dbNama = snapshot.child("nama").value.toString()
                            val dbRole = snapshot.child("role").value.toString()
                            
                            tvNama.text = dbNama
                            tvRole.text = dbRole
                            tvEmail.text = email

                            // Simpan ke SharedPreferences agar kedepannya cepat
                            val editor = shared.edit()
                            editor.putString("nama", dbNama)
                            editor.putString("role", dbRole)
                            editor.apply()
                        } else {
                            tvNama.text = email.split("@")[0]
                            tvEmail.text = email
                            tvRole.text = "Admin"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@AkunActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
