package com.example.hw4

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesScrollable : Fragment() {
    private lateinit var fireBaseDB: FirebaseFirestore
    private lateinit var dbRef: DatabaseReference
    private var TAG = "FirebaseScrollable"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fireBaseDB = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        return inflater.inflate(R.layout.fragment_recycler, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[InfoViewModel::class.java]
        var results = ""
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userInfo = currentUser!!.uid

        dbRef = FirebaseDatabase.getInstance().getReference(userInfo)

        fireBaseDB.collection(currentUser.uid)
            .orderBy("DateAndTime")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val favorites = snapshots.toObjects(Favorites::class.java)
                    val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
                    results = if(favorites.isEmpty()) {
                        "You don't have any favorite events yet :("
                    } else {
                        ""
                    }
                    if (recyclerView != null) {
                        val adapter = FavoritesAdapter(favorites)
                        recyclerView.adapter = FavoritesAdapter(favorites)
                        recyclerView.layoutManager = LinearLayoutManager(this.context)
                        adapter.notifyDataSetChanged()
                    }
                }
                viewModel.setResults(results)
                Log.d(TAG, "result: $results")
            }
    }
}