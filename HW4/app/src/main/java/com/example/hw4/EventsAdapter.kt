package com.example.hw4

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventsAdapter(private val eventsInfo: ArrayList<EventsInfo>?): RecyclerView.Adapter<EventsAdapter.MyViewHolder>() {
    private val dateTimeBuild = StringBuilder()
    private lateinit var formattedDateTime: String
    private lateinit var eventTicketInfo: String
    private val eventLinks: ArrayList<String> = arrayListOf()
    private val favoriteList: ArrayList<String> = arrayListOf()
    private lateinit var fireBaseDB: FirebaseFirestore
    private var imageLink: ArrayList<String> = arrayListOf()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userInfo = currentUser!!.uid

    val TAG = "EventsAdapter"
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val eventName = itemView.findViewById<TextView>(R.id.event_name)
        val eventVenue = itemView.findViewById<TextView>(R.id.event_venue)
        val eventAddress = itemView.findViewById<TextView>(R.id.event_address)
        val eventDateTime = itemView.findViewById<TextView>(R.id.event_date)
        val eventPrice = itemView.findViewById<TextView>(R.id.event_price)
        val eventImage = itemView.findViewById<ImageView>(R.id.event_image)
        private val ticketButton = itemView.findViewById<Button>(R.id.ticket_button)
        val favoriteEvents = itemView.findViewById<CheckBox>(R.id.cb_favorite)

        init {
            favoriteEvents.setOnClickListener {
                val selectedItem = adapterPosition

                favoriteList.add(selectedItem.toString())
                fireBaseDB = FirebaseFirestore.getInstance()
                val eventDB = fireBaseDB.collection(userInfo)

                if(favoriteEvents.isChecked) {
                    val event = hashMapOf(
                        "EventName" to eventName.text.toString(),
                        "EventVenue" to eventVenue.text.toString(),
                        "Address" to eventAddress.text.toString(),
                        "DateAndTime" to eventDateTime.text.toString(),
                        "Price" to eventPrice.text.toString(),
                        "Image" to imageLink[selectedItem],
                        "Link" to eventTicketInfo
                    )
                    val documentId = eventDB.document().id
                    eventDB.document(documentId).set(event)
                } else {
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
        } // Launches the ticket information on the web when user clicks the 'See Tickets' button
    } // Initializes variables

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return eventsInfo!!.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = eventsInfo?.get(position)
        if (currentItem != null) {
            holder.eventName.text = currentItem.name
            holder.eventVenue.text = "${currentItem.locationInfo?.venues?.get(0)?.venueName}, ${currentItem.locationInfo?.venues?.get(0)?.city?.cityName}"
            holder.eventAddress.text = "Address: ${currentItem.locationInfo?.venues?.get(0)?.address?.eventAddress}," +
                        " ${currentItem.locationInfo?.venues?.get(0)?.city?.cityName}, ${currentItem.locationInfo?.venues?.get(0)?.state?.stateName}"

            formatDateTime(currentItem)
            formattedDateTime = dateTimeBuild.toString()
            holder.eventDateTime.text = "Date: $formattedDateTime"
            dateTimeBuild.clear()

            val context = holder.itemView.context
            val highestQualityImage = currentItem.images.maxByOrNull {
                it.width * it.height
            }
            Glide.with(context)
                .load(highestQualityImage?.imageUrl)
                .into(holder.eventImage)

            imageLink.add(highestQualityImage?.imageUrl.toString())

            eventTicketInfo = currentItem.url
            eventLinks.add(eventTicketInfo)

            fireBaseDB = FirebaseFirestore.getInstance()
            fireBaseDB.collection(userInfo)
                .whereEqualTo("EventName", holder.eventName.text)
                .whereEqualTo("DateAndTime", holder.eventDateTime.text.toString())
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        favoriteList.add(position.toString())
                        if(favoriteList.contains(position.toString())) {
                            holder.favoriteEvents.setChecked(true)
                        }
                    }
                }

            if(!favoriteList.contains(position.toString())) {
                holder.favoriteEvents.setChecked(false)
            }
        }

        val getMinPrice = currentItem?.priceRanges?.get(0)?.minPrice
        val getMaxPrice = currentItem?.priceRanges?.get(0)?.maxPrice
        holder.eventPrice.text = if(getMinPrice == null || (getMinPrice == 0.0 && getMaxPrice == 0.0)) {
            "Price Not Listed"
        } else if(getMinPrice != getMaxPrice){
            "$$getMinPrice - $$getMaxPrice"
        } else {
            "$$getMinPrice"
        }
    } // Assigns the variables to data in the data class

    private fun formatDateTime(currentItem: EventsInfo) {
        val date = currentItem.dates.start.localDate
        val time = currentItem.dates.start.localTime
        val year = StringBuilder()
        val dayMonth = StringBuilder()
        var check = 0

        for((i, index) in date.withIndex()) {
            if(date[i] == '-') {
                check++
                if(check > 1) {
                    dayMonth.append("/")
                }
            } else {
                if(check == 0) {
                    year.append(index)
                } else {
                    dayMonth.append(index)
                }
            }
        }
        dateTimeBuild.append("$dayMonth/$year")

        if(time != null) {
            check = 0
            val hours = StringBuilder()
            val minutes = StringBuilder()
            for((i, index) in time.withIndex()) {
                if(time[i] == ':') {
                    check++
                } else if(check == 2) {
                    break
                } else {
                    if(check == 0) {
                        hours.append(index)
                    } else {
                        minutes.append(index)
                    }
                }
            }

            var hoursNum: Int = hours.toString().toInt()
            var meridiem = "AM"
            if (hoursNum < 12) {
            } else if(hoursNum == 12) {
                meridiem = "PM"
            } else if (hoursNum == 24) {
                hoursNum -= 12
            } else {
                hoursNum -= 12
                meridiem = "PM"
            }
            dateTimeBuild.append(" @ $hoursNum:$minutes $meridiem")
        }
    } // Formats the date and time into a single string
}
