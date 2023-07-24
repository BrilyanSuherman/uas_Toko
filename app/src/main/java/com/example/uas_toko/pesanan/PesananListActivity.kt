package com.example.uas_toko.pesanan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uas_toko.databinding.ActivityPesananListBinding
import com.example.uas_toko.produk.Produk
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PesananListActivity : AppCompatActivity () {

    private lateinit var binding: ActivityPesananListBinding

    private lateinit var pesananRecyclerView: RecyclerView
    private lateinit var pesananArrayList: ArrayList<Produk>
    private lateinit var pesananAdapter: PesananAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPesananListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pesananRecyclerView = binding.produkListView
        pesananRecyclerView.layoutManager = LinearLayoutManager(this)
        pesananRecyclerView.setHasFixedSize(true)

        pesananArrayList = arrayListOf()
        pesananAdapter = PesananAdapter(pesananArrayList)

        pesananRecyclerView.adapter = pesananAdapter

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

    }

    private fun load_data() {
        pesananArrayList.clear()
        db = FirebaseFirestore.getInstance()
        db.collection("produk").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED)
                        pesananArrayList.add(dc.document.toObject(Produk::class.java))
                }
                pesananAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun search_data(keyword :String) {
        pesananArrayList.clear()

        db = FirebaseFirestore.getInstance()

        val query = db.collection("produk")
            .orderBy("nama")
            .startAt(keyword)
            .get()
        query.addOnSuccessListener {
            pesananArrayList.clear()
            for (document in it) {
                pesananArrayList.add(document.toObject(Produk::class.java))
            }
            pesananAdapter.notifyDataSetChanged()
        }
    }

    private fun deletePesanan(produk: Produk, doc_id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Apakah${produk.nama} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("yes") { dialog, id ->
                lifecycleScope.launch {
                    db.collection("pesanan")
                        .document(doc_id).delete()

                    Toast.makeText(
                        applicationContext, produk.nama.toString() + "is Deleted",
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
                    val produk = pesananArrayList[position]
                    val personQuery = db.collection("pesanan")
                        .whereEqualTo("nama", produk.nama)
                        .whereEqualTo("kode", produk.kode)
                        .whereEqualTo("namaproduk", produk.namaproduk)
                        .whereEqualTo("harga", produk.harga)
                        .get()
                        .await()

                    if (personQuery.documents.isNotEmpty()) {
                        for (document in personQuery) {
                            try {
                                deletePesanan(produk, document.id)
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
                                applicationContext, "Pesanan yang ingin anda hapus tidak ditemukan",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }).attachToRecyclerView(pesananRecyclerView)
    }


}