package com.example.uas_toko.pesanan

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.uas_toko.databinding.ActivityDetailPesananBinding
import com.example.uas_toko.produk.Produk
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PesananDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailPesananBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val (curr_produk) = setDefaultValue()


        showFoto()
    }

    fun setDefaultValue(): Array<Any> {
        val intent = intent
        val kode = intent.getStringExtra("kode").toString()
        val namaproduk = intent.getStringExtra("namaproduk").toString()
        val harga = intent.getStringExtra("harga").toString()


        binding.TVLkode.setText(kode)
        binding.TVLNamaProduk.setText(namaproduk)
        binding.TVLHarga.setText(harga)

        val curr_produk = Produk(kode, namaproduk, harga)
        return arrayOf(curr_produk)

    }

    fun showFoto(){
        val intent = intent
        val kode = intent.getStringExtra("kode").toString()
        val namaproduk = intent.getStringExtra("namaproduk").toString()


        val storageRef = FirebaseStorage.getInstance().reference.child("img_produk/${kode}_${namaproduk}.jpg")
        val localfile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.IMLGambarMakanan.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Log.e("foto ?", "gagal")
        }

    }

    }

