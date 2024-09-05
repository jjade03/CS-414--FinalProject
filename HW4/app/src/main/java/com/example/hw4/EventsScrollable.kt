package com.example.hw4

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EventsScrollable : Fragment() {
    private val TAG = "EventsScrollableFragment"
    private val BASE_URL = "https://app.ticketmaster.com/discovery/v2/"
    private val apiKey = "zY4fjGLQ9v61WAe5JbsGNEyfjcdBgrcm"
    private val sortBy = "date,asc"
    private var cityName: String = ""
    private var eventName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val eventList = ArrayList<EventsInfo>()
        var viewModel: InfoViewModel

        recyclerView.apply {
            val adapter = EventsAdapter(eventList)

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this.context)

            val retroFit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            viewModel = ViewModelProvider(requireActivity())[InfoViewModel::class.java]
            cityName = viewModel.returnCity()
            eventName = viewModel.returnEvent()

            val getEventInfo = retroFit.create(GetEventInfo::class.java)
            if(cityName != "" && eventName != "") {
                getEventInfo.getInfoOfEvents(20, eventName, cityName, apiKey, sortBy)
                .enqueue(object : Callback<EventInfo> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(
                        call: Call<EventInfo>,
                        response: Response<EventInfo>
                    ) {
                        Log.d(TAG, "onResponse: $response")

                        val body = response.body()
                        if (body == null) {
                            Log.w(TAG, "Valid response was not received")
                            return
                        }

                        val results: String
                        if (body._embedded?.events == null) {
                            results = "No results were found :("
                        } else {
                            results = ""
                            eventList.addAll(body._embedded.events)
                        }
                        viewModel.setResults(results)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onFailure(call: Call<EventInfo?>, t: Throwable) {
                        Log.d(TAG, "onFailure: $t")
                    }
                })
            }
        }  // Obtains the data passed into the recycler view adapter and displays the recycler view
    }
}