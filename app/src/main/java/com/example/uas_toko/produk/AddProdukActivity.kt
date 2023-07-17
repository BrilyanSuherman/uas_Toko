package com.example.uas_toko.produk

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.uas_toko.MainActivity
import com.example.uas_toko.databinding.ActivityAddProdukBinding
import com.example.uas_toko.databinding.ActivitySettingBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File

class AddProdukActivity : AppCompatActivity () {

    private lateinit var binding: ActivityAddProdukBinding
    private val firestoreDatabase =FirebaseFirestore.getInstance()

    private val GALLERY_REQUEST_CODE = 100
    private val REQ_CAM = 101
    private lateinit var imgUri : Uri
    private var datagambar: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProdukBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BtnAddProduk.setOnClickListener { addProduk()  }

        binding.BtnImgProduk.setOnClickListener{ openCamera() }

        showFoto()
    }

    fun addProduk(){
        var kode : String = binding.TxtAddKode.text.toString()
        var namaproduk : String = binding.TxtAddNamaProduk.text.toString()
        var harga : String = binding.TxtAddHarga.text.toString()

        val produk: MutableMap<String, Any> = HashMap ()
        produk["kode"] = kode
        produk["namaproduk"] = namaproduk
        produk["harga"] = harga

        if (datagambar != null) {
            uploadPictFirebase(datagambar!!, "${kode}_${namaproduk}")
        }

        firestoreDatabase.collection("produk").add(produk)
            .addOnSuccessListener {
                val intentMain = Intent(this, MainActivity::class.java)
                startActivity(intentMain)
            }
    }

    fun showFoto() {
        val intent = intent
        val kode = intent.getStringExtra("kode").toString()
        val namaproduk = intent.getStringExtra("namaproduk").toString()

        val storageRef = FirebaseStorage.getInstance().reference.child("img_produk/${kode}_${namaproduk}.jpg")
        val localfile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.BtnImgProduk.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.e("foto ?", "gagal")
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAM && resultCode == RESULT_OK) {
            datagambar = data?.extras?.get("data") as Bitmap
            binding.BtnImgProduk.setImageBitmap(datagambar)
        }
    }

//    private fun openGallery() {
//        Intent(MediaStore.ACTION_PICK_IMAGES).also { intent ->
//            this.packageManager?.let {
//                intent?.resolveActivity(it).also {
//                    startActivityForResult(intent, GALLERY_REQUEST_CODE)
//                }
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
//            val imageUri = data?.data
//            imageUri?.let {
//                // Handle the selected image from the gallery
//                val bitmap: Bitmap? = MediaStore.Images.Media.getBitmap(contentResolver, it)
//                // TODO: Upload the image to Firebase storage or perform any other action
//            }
//        }
//    }

    private fun uploadPictFirebase(img_bitmap: Bitmap, file_name: String) {
        val baos = ByteArrayOutputStream()
        val ref = FirebaseStorage.getInstance().reference.child("img_produk/${file_name}.jpg")
        img_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val img = baos.toByteArray()
        ref.putBytes(img)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener { Task ->
                        Task.result.let { Uri ->
                            imgUri = Uri
                            binding.BtnImgProduk.setImageBitmap(img_bitmap)
                        }
                    }
                }
            }
    }
}