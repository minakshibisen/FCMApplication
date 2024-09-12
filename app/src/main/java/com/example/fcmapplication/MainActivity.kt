package com.example.fcmapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteFragment
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var myMap: GoogleMap? = null
    private lateinit var mapView: MapView
    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

       Places.initializeWithNewPlacesApiEnabled(applicationContext, getString(R.string.my_map_api_key))
//        places.initialize(applicationContext, getString(R.string.my_map_api_key))
       autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                if (latLng != null) {
                    zoomOnMap(latLng)
                }
                // Handle the selected place
                Toast.makeText(this@MainActivity, "Place: ${place.address}, ${place.id}", Toast.LENGTH_SHORT).show()
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
                // Handle the error
                Toast.makeText(this@MainActivity, "An error occurred: $status", Toast.LENGTH_SHORT).show()
            }
        })


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1001
        )

        FirebaseApp.initializeApp(this)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            FirebaseMessaging.getInstance().subscribeToTopic("general")
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            Log.e("TAG", "onCreate() called with: task = ${task.result}")
        }
    }
private fun zoomOnMap(latLng: LatLng){
    val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng,12f)
    myMap!!.animateCamera(newLatLngZoom)
}

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        // Set a click listener for map clicks
        myMap!!.setOnMapClickListener { latLng ->
            openLocationInGoogleMaps(latLng)
        }
        // Add a marker at a location and move the camera
        val indore = LatLng(22.719568, 75.857727)
        myMap!!.addMarker(MarkerOptions().position(indore).title("Marker in Indore"))
        myMap!!.moveCamera(CameraUpdateFactory.newLatLng(indore))
    }

    private fun openLocationInGoogleMaps(latLng: LatLng) {
        val uri =
            Uri.parse("geo:${latLng.latitude},${latLng.longitude}?q=${latLng.latitude},${latLng.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

}