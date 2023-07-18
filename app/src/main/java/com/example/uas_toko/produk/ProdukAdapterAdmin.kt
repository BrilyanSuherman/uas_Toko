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
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProdukAdapterAdmin (private val produkList: ArrayList<Produk>) :
    RecyclerView.Adapter<ProdukAdapterAdmin.ProdukViewHolder>() {

    private lateinit var activity:AppCompatActivity

    class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val namaproduk: TextView = itemView.findViewById(R.id.TVLNamaProduk)
        val harga: TextView = itemView.findViewById(R.id.TVLHarga)

        val img_produk: ImageView = itemView.findViewById(R.id.IMLGambarMakanan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.produk_list_layout_admin, parent, false)
        return ProdukViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: ProdukAdapterAdmin.ProdukViewHolder, position: Int) {
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
            activity.startActivity(Intent(activity, EditProdukDetailActivity::class.java).apply {
                putExtra("kode", produk.kode.toString())
                putExtra("namaproduk", produk.namaproduk.toString())
                putExtra("harga", produk.harga.toString())
            })

        }
    }

    override fun getItemCount(): Int {
        return produkList.size
    }
}