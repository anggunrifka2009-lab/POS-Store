package com.anggun.pos

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AkunActivity : AppCompatActivity() {

    private lateinit var tvNama: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var btnLogout: Button

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_akun)

        database = FirebaseDatabase.getInstance().reference

        tvNama = findViewById(R.id.tvNamaProfile)
        tvEmail = findViewById(R.id.tvEmailProfile)
        tvRole = findViewById(R.id.tvRoleProfile)
        ivProfile = findViewById(R.id.ivProfile)
        btnLogout = findViewById(R.id.btnLogout)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        loadProfile()

        ivProfile.setOnClickListener {
            showDialogEditFoto()
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
            shared.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadProfile() {
        val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
        val nama = shared.getString("nama", null)
        val role = shared.getString("role", null)
        val foto = shared.getString("foto", null)
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "-"

        if (nama != null && role != null) {
            tvNama.text = nama
            tvEmail.text = email
            tvRole.text = role

            if (!foto.isNullOrEmpty()) {
                Glide.with(this)
                    .load(foto)
                    .placeholder(R.drawable.img_11)
                    .error(R.drawable.img_11)
                    .into(ivProfile)
            }
        } else if (user != null) {
            database.child("akun").child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dbNama = snapshot.child("nama").value.toString()
                            val dbRole = snapshot.child("role").value.toString()
                            val dbFoto = snapshot.child("foto").value?.toString()

                            tvNama.text = dbNama
                            tvRole.text = dbRole
                            tvEmail.text = email

                            if (!dbFoto.isNullOrEmpty()) {
                                Glide.with(this@AkunActivity)
                                    .load(dbFoto)
                                    .placeholder(R.drawable.img_11)
                                    .error(R.drawable.img_11)
                                    .into(ivProfile)
                            }

                            val editor = shared.edit()
                            editor.putString("nama", dbNama)
                            editor.putString("role", dbRole)
                            editor.putString("foto", dbFoto)
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

    private fun showDialogEditFoto() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_foto, null)
        val etLink = dialogView.findViewById<EditText>(R.id.etLinkFoto)

        val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
        etLink.setText(shared.getString("foto", ""))

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("Simpan") { d, _ ->
                val newFoto = etLink.text.toString().trim()
                updateFoto(newFoto)
                d.dismiss()
            }
            .setNegativeButton("Batal") { d, _ ->
                d.cancel()
            }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(android.R.color.darker_gray))
    }

    private fun updateFoto(url: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            database.child("akun").child(user.uid).child("foto").setValue(url)
                .addOnSuccessListener {
                    Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()

                    val shared = getSharedPreferences("LOGIN", MODE_PRIVATE)
                    shared.edit().putString("foto", url).apply()

                    if (url.isNotEmpty()) {
                        Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.img_11)
                            .error(R.drawable.img_11)
                            .into(ivProfile)
                    } else {
                        ivProfile.setImageResource(R.drawable.img_11)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memperbarui foto: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}