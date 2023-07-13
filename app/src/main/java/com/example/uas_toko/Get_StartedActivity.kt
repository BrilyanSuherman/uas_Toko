package com.example.uas_toko

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uas_toko.auth.SignInActivity
import com.example.uas_toko.auth.SignUpActivity
import com.example.uas_toko.databinding.ActivityGetStartedBinding
import com.google.firebase.auth.FirebaseAuth

class Get_StartedActivity : AppCompatActivity () {

    private lateinit var binding: ActivityGetStartedBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnDaftar.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.btnMasuk.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

    }

}