package com.example.uas_toko.produk

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.uas_toko.R
import com.example.uas_toko.pesanan.AddPesananActivity
import com.example.uas_toko.databinding.ProdukListLayoutBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProdukAdapter (private val produkList: ArrayList<Produk>) :
    RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    private lateinit var activity:AppCompatActivity

    private lateinit var binding: ProdukListLayoutBinding



        class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val namaproduk: TextView = itemView.findViewById(R.id.TVLNamaProduk)
            val harga:  TextView = itemView.findViewById(R.id.TVLHarga)

            val img_produk : ImageView = itemView.findViewById(R.id.IMLGambarMakanan)
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.produk_list_layout,parent,false)
        return ProdukViewHolder(itemView)


    }


    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val produk: Produk = produkList[position]
        holder.namaproduk.text = produk.namaproduk
        holder.harga.text = produk.harga

        val storageRef =
            FirebaseStorage.getInstance().reference.child("img_produk/${produk.kode}_${produk.namaproduk}.jpg")
        val localfile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            holder.img_produk.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.e("foto ?", "gagal")
        }

    holder.itemView.setOnClickListener{
        activity = it.context as AppCompatActivity
        activity.startActivity(Intent(activity, ProdukDetailActivity::class.java).apply {
            putExtra("kode", produk.kode.toString())
            putExtra("namaproduk", produk.namaproduk.toString())
            putExtra("harga", produk.harga.toString())
        })

    }

//        binding.BtnBeli.setOnClickListener {
//            activity = it.context as AppCompatActivity
//            activity.startActivity(Intent(activity, AddPesananActivity::class.java))
//
//        }
}

    override fun getItemCount(): Int {
        return produkList.size
    }
}