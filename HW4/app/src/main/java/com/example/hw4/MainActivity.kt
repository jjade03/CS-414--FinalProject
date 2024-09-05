package com.example.hw4

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(){
    private val viewModel: InfoViewModel by viewModels()
    private var firstFragment = false
    private var eventName: String = ""
    private var cityName: String = ""
    private lateinit var resultsCheck: TextView

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if(currentUser == null) {
            Log.d(TAG, "user is null")
            loginRequest()
        } else {
            Log.d(TAG, "user logged in")
        }

        // Sets the default header using fragments
        supportFragmentManager.beginTransaction()
            .replace(R.id.headerFragmentContainer, SearchHeaderFragment())
            .addToBackStack(null)
            .commit()

        // Sets up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Receives results from fragments
        resultsCheck = findViewById(R.id.no_results)
        viewModel.passedResult.observe(this) {item ->
            resultsCheck.text = item
        } // Referenced code from this article:
          // https://medium.com/androiddevelopers/viewmodels-and-livedata-patterns-antipatterns-21efaef74a54
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_logout -> {
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener {task->
                        if(task.isSuccessful) {
                            loginRequest()
                        } else {
                            Log.d(TAG, "unsuccessful")
                        }
                    }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun loginRequest() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    } // Launches the LoginActivity

    fun saveSearchParameters(view: View) {
        val errorTitle: String
        val errorMessage: String
        val eventText = findViewById<EditText>(R.id.event_search)
        val cityText = findViewById<EditText>(R.id.city_search)

        cityName = cityText.getText().toString()
        eventName = eventText.getText().toString()

        viewModel.setEvent(eventName)
        viewModel.setCity(cityName)
        when {
            eventName == "" && cityName == "" -> {
                errorTitle = "No Search Terms Specified"
                errorMessage = "A city and search term must be provided. Please enter all search parameters."
                dialogMessages(errorTitle, errorMessage)
            }
            eventName == "" -> {
                errorTitle = "Search Term Missing"
                errorMessage = "Search term cannot be empty. Please enter a search term."
                dialogMessages(errorTitle, errorMessage)
            }
            cityName == "" -> {
                errorTitle = "Location Missing"
                errorMessage = "City cannot be empty. Please enter a city."
                dialogMessages(errorTitle, errorMessage)
            }
            else ->  displayRecyclerView()
        } // Checks if the keyword or city is empty and responds accordingly
        eventText.hideKeyboard()
        cityText.hideKeyboard()
    } // Saves the search parameters using shared preferences

    private fun View.hideKeyboard() {
        val hide = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        hide.hideSoftInputFromWindow(windowToken, 0)
    } // Function to hide keyboard

    private fun dialogMessages(errorTitle: String, errorMessage: String) {
        val builder = AlertDialog.Builder(this)
        builder.setIcon(android.R.drawable.ic_delete)
        builder.setTitle(errorTitle)
        builder.setPositiveButton("OK") { _, _ ->
        }
        builder.setMessage(errorMessage)
        val dialog = builder.create()
        dialog.show()
    } // If the keyword and/or city is empty, displays dialog with an appropriate error message

    private fun displayRecyclerView() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.recyclerFragmentContainer, EventsScrollable())
            .addToBackStack(null)
            .commit()
    } // Displays the search RecyclerView when appropriate search parameters are entered

    fun swapFragments(view: View) {
        val transaction = supportFragmentManager.beginTransaction()

        resultsCheck = findViewById(R.id.no_results)
        resultsCheck.text = "" // Sets the text to nothing to prevent any unnecessary carryover

        firstFragment = if(firstFragment) {
            transaction.replace(R.id.headerFragmentContainer, SearchHeaderFragment())
            transaction.replace(R.id.recyclerFragmentContainer, EventsScrollable())
            false
        } else {
            transaction.replace(R.id.headerFragmentContainer, FavoritesHeaderFragment())
            transaction.replace(R.id.recyclerFragmentContainer, FavoritesScrollable())
            true
        }
        transaction.commit()
        Log.d(TAG, "event name: $eventName, city name: $cityName")
    } // Swaps between the two fragments when the buttons in the top right are clicked
}