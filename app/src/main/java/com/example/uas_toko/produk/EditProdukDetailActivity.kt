package com.example.uas_toko.produk

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uas_toko.admin.AdminActivity
import com.example.uas_toko.databinding.ActivityEditProdukDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class EditProdukDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProdukDetailBinding
    private val db = FirebaseFirestore.getInstance()

    private val GALLERY_REQUEST_CODE = 1
    private val REQ_CAM = 101
    private var imgUri: Uri? = null
    private var datagambar: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProdukDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val curr_produk = setDefaultValue()

        // Handle the Save button click
        binding.btnSaveProduk.setOnClickListener {
            val new_data_produk = newProduk()
            updateProduk(curr_produk as Produk, new_data_produk)

            val intentMain = Intent(this, AdminActivity::class.java)
            startActivity(intentMain)
            finish()
        }

        binding.btnImgProduk.setOnClickListener { openGallery() }

        showFoto()
    }

//            val kode = binding.edtKodeProduk.text.toString() // Get the updated kode
//            val namaproduk = binding.edtNamaProduk.text.toString()
//            val harga = binding.edtHarga.text.toString()
//
//            // Update the data in Firestore or other data source
//            updateProduk(kode, namaproduk, harga)
//
//            // Finish the activity after saving changes
//            finish()
//        }


     fun setDefaultValue(): Array<Any> {
        val intent = intent
        val kode = intent.getStringExtra("kode").toString()
        val namaproduk = intent.getStringExtra("namaproduk").toString()
        val harga = intent.getStringExtra("harga").toString()

        binding.edtKodeProduk.setText(kode)
        binding.edtNamaProduk.setText(namaproduk)
        binding.edtHarga.setText(harga)

         val curr_produk = Produk(kode, namaproduk, harga)
         return arrayOf(curr_produk)
    }

    fun newProduk(): Map<String, Any> {
        var kode: String = binding.edtKodeProduk.text.toString()
        var namaproduk: String = binding.edtKodeProduk.text.toString()
        var harga: String = binding.edtHarga.text.toString()

        if (datagambar != null) {
            uploadPictFirebase(datagambar!!, "${kode}_${namaproduk}")
        }

        val produk = mutableMapOf<String, Any>()
        produk["kode"] = kode
        produk["namaproduk"] = namaproduk
        produk["harga"] = harga

        return produk
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // Ubah sesuai dengan jenis file yang ingin diunggah
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }
    fun showFoto() {
        val intent = intent
        val kode = intent.getStringExtra("kode").toString()
        val namaproduk = intent.getStringExtra("namaproduk").toString()

        val storageRef =
            FirebaseStorage.getInstance().reference.child("img_produk/${kode}_${namaproduk}.jpg")
        val localfile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            binding.btnImgProduk.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.e("foto ?", "gagal")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { imageUri ->
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    datagambar = bitmap
                    binding.btnImgProduk.setImageBitmap(datagambar)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
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
                            binding.btnImgProduk.setImageBitmap(img_bitmap)

                            // Lakukan operasi lain dengan URL gambar jika diperlukan
                        }
                    }
                }
            }
    }

    private fun updateProduk(produk: Produk, newProdukMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            val personQuery = db.collection("produk")
                .whereEqualTo("kode", produk.kode)
                .whereEqualTo("namaproduk", produk.namaproduk)
                .whereEqualTo("harga", produk.harga)
                .get()
                .await()
            if (personQuery.documents.isNotEmpty()) {
                for (document in personQuery) {
                    try {
                        db.collection("produk").document(document.id).set(
                            newProdukMap,
                            SetOptions.merge()
                        )
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@EditProdukDetailActivity,
                                e.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EditProdukDetailActivity,
                        "No person matched the query.", Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
}