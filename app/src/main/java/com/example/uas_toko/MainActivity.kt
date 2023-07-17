package com.example.uas_toko

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uas_toko.R
import com.example.uas_toko.auth.SettingActivity
import com.example.uas_toko.chat.ChatActivity
import com.example.uas_toko.databinding.ActivityMainBinding
import com.example.uas_toko.produk.AddProdukActivity
import com.example.uas_toko.produk.Produk
import com.example.uas_toko.produk.ProdukAdapter
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var produkRecylerView: RecyclerView
    private lateinit var produkArrayList: ArrayList<Produk>
    private lateinit var produkAdapter: ProdukAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        produkRecylerView = binding.produkListView
        produkRecylerView.layoutManager = LinearLayoutManager(this)
        produkRecylerView.setHasFixedSize(true)

        produkArrayList = arrayListOf()
        produkAdapter = ProdukAdapter(produkArrayList)

        produkRecylerView.adapter = produkAdapter

        binding.btnAddProduk.setOnClickListener {
            val intentMain = Intent(this, AddProdukActivity::class.java)
            startActivity(intentMain)
        }

        load_data()

        binding.txtSearchProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val keyword = binding.txtSearchProduk.text.toString()
                if (keyword.isNotEmpty()) {
                    search_data(keyword)
                } else {
                    load_data()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        swipeDelete()

        binding.bottomNavigation.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.nav_bottom_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_bottom_setting -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_bottom_chat -> {
                    val intent = Intent(this, ChatActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun load_data() {
        produkArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("produk").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED)
                        produkArrayList.add(dc.document.toObject(Produk::class.java))
                }
                produkAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun search_data(keyword :String) {
        produkArrayList.clear()

        db = FirebaseFirestore.getInstance()

        val query = db.collection("produk")
            .orderBy("namaproduk")
            .startAt(keyword)
            .get()
        query.addOnSuccessListener {
            produkArrayList.clear()
            for (document in it) {
                produkArrayList.add(document.toObject(Produk::class.java))
            }
            produkAdapter.notifyDataSetChanged()
        }
    }

    private fun deleteProduk(produk: Produk, doc_id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Apakah${produk.namaproduk} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("yes") { dialog, id ->
                lifecycleScope.launch {
                    db.collection("produk")
                        .document(doc_id).delete()

                    deleteFoto("img_produk/${produk.kode}_${produk.namaproduk}.jpg")

                    Toast.makeText(
                        applicationContext, produk.namaproduk.toString() + "is Deleted",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    load_data()
                }
            }
            .setNegativeButton("NO") { dialog, id ->
                dialog.dismiss()
                load_data()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                lifecycleScope.launch {
                    val produk = produkArrayList[position]
                    val personQuery = db.collection("produk")
                        .whereEqualTo("kode", produk.kode)
                        .whereEqualTo("namaproduk", produk.namaproduk)
                        .whereEqualTo("harga", produk.harga)
                        .get()
                        .await()

                    if (personQuery.documents.isNotEmpty()) {
                        for (document in personQuery) {
                            try {
                                deleteProduk(produk, document.id)
                                load_data()
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        applicationContext,
                                        e.message.toString(), Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext, "Produk yang ingin anda hapus tidak ditemukan",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }).attachToRecyclerView(produkRecylerView)
    }

    private fun deleteFoto(file_name: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val deleteFileRef = storageRef.child(file_name)
        if (deleteFileRef != null) {
            deleteFileRef.delete().addOnSuccessListener {
                Log.e("deleted", "success")
            }.addOnFailureListener {
                Log.e("deleted", "failed")
            }
        }
    }
}






