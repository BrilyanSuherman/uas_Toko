package com.example.uas_toko.pesanan

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
import com.example.uas_toko.produk.Produk
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PesananAdapter (private val pesananList: ArrayList<Produk>) :
    RecyclerView.Adapter<PesananAdapter.PesananViewHolder>() {

    private lateinit var activity: AppCompatActivity

    class PesananViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama : TextView = itemView.findViewById(R.id.TVLNama)
        val namaproduk: TextView = itemView.findViewById(R.id.TVLNamaProduk)
        val harga: TextView = itemView.findViewById(R.id.TVLHarga)
        val img_produk : ImageView = itemView.findViewById(R.id.IMLGambarMakanan)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.pesanan_list_layout,parent,false)
        return PesananViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: PesananViewHolder, position: Int) {
        val produk: Produk = pesananList[position]
        holder.nama.text = produk.nama
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
            activity.startActivity(Intent(activity, PesananDetailActivity::class.java).apply {
                putExtra("nama", produk.nama.toString())
                putExtra("kode", produk.kode.toString())
                putExtra("namaproduk", produk.namaproduk.toString())
                putExtra("harga", produk.harga.toString())
//            putExtra("jenis_kelamin", pasien.jenis_kelamin.toString())
//            putExtra("penyakit_bawaan", pasien.penyakit_bawaan.toString())
            })

        }
    }

    override fun getItemCount(): Int {
        return pesananList.size
    }
}