package com.example.uas_toko.produk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uas_toko.databinding.ActivityEditProdukDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class EditProdukDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProdukDetailBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProdukDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val curr_produk = setDefaultValue()

        // Handle the Save button click
        binding.btnSaveProduk.setOnClickListener {
            val kode = binding.edtKodeProduk.text.toString() // Get the updated kode
            val namaproduk = binding.edtNamaProduk.text.toString()
            val harga = binding.edtHarga.text.toString()

            // Update the data in Firestore or other data source
            updateProduk(kode, namaproduk, harga)

            // Finish the activity after saving changes
            finish()
        }
    }

    private fun setDefaultValue(): Produk {
        val intent = intent
        val kode = intent.getStringExtra("kode").toString()
        val namaproduk = intent.getStringExtra("namaproduk").toString()
        val harga = intent.getStringExtra("harga").toString()

        binding.edtKodeProduk.setText(kode)
        binding.edtNamaProduk.setText(namaproduk)
        binding.edtHarga.setText(harga)

        return Produk(kode, namaproduk, harga)
    }

    private fun updateProduk(kode: String, namaproduk: String, harga: String) {
        // Update the data in Firestore or other data source
        val produkRef = db.collection("produk").document(kode)
        produkRef.update(
            mapOf(
                "namaproduk" to namaproduk,
                "harga" to harga
            )
        )
            .addOnSuccessListener {
                // Handle successful update if needed
            }
            .addOnFailureListener {
                // Handle error if needed
            }
    }
}
