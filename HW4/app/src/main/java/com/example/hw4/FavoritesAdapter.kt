package com.example.hw4

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FavoritesAdapter(private val favorites: MutableList<Favorites>): RecyclerView.Adapter<FavoritesAdapter.MyViewHolder>() {
    private lateinit var eventTicketInfo: String
    private val eventLinks: ArrayList<String> = arrayListOf()
    private lateinit var fireBaseDB: FirebaseFirestore

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val eventName = itemView.findViewById<TextView>(R.id.event_name)
        val eventVenue = itemView.findViewById<TextView>(R.id.event_venue)
        val eventAddress = itemView.findViewById<TextView>(R.id.event_address)
        val eventDateTime = itemView.findViewById<TextView>(R.id.event_date)
        val eventPrice = itemView.findViewById<TextView>(R.id.event_price)
        val eventImage = itemView.findViewById<ImageView>(R.id.event_image)
        private val ticketButton = itemView.findViewById<Button>(R.id.ticket_button)
        val favoriteEvents = itemView.findViewById<CheckBox>(R.id.cb_favorite)

        init{
            favoriteEvents.setOnClickListener {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val userInfo = currentUser!!.uid
                fireBaseDB = FirebaseFirestore.getInstance()
                if(!favoriteEvents.isChecked) {
                    fireBaseDB.collection(userInfo)
                        .whereEqualTo("EventName", eventName.text.toString())
                        .whereEqualTo("DateAndTime", eventDateTime.text.toString())
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                if (document != null) {
                                    document.reference.delete()
                                    break
                                }
                            }
                        }
                }
            }

            ticketButton.setOnClickListener {
                val selectedItem = adapterPosition
                for((i, _) in eventLinks.withIndex()) {
                    if(i == selectedItem) {
                        eventTicketInfo = eventLinks[i]
                    }
                }
                val ticketBrowser = Intent(Intent.ACTION_VIEW)
                ticketBrowser.data = Uri.parse(eventTicketInfo)
                itemView.context.startActivity(ticketBrowser) // Opens the ticket browser
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return favorites.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = favorites[position]

        holder.eventName.text = currentItem.EventName
        holder.eventVenue.text = currentItem.EventVenue
        holder.eventAddress.text = currentItem.Address
        holder.eventDateTime.text = currentItem.DateAndTime
        holder.eventPrice.text = currentItem.Price

        val context = holder.itemView.context
        Glide.with(context)
            .load(currentItem.Image)
            .into(holder.eventImage)

        eventTicketInfo = currentItem.Link.toString()
        eventLinks.add(eventTicketInfo)

        holder.favoriteEvents.setChecked(true)
    }
}
