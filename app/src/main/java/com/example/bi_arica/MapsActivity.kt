package com.example.bi_arica

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedEventType: Int = 1
    private var lastHeatmapOverlay: TileOverlay? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val spinner: Spinner = findViewById(R.id.event_types)
        val eventTypes = getEventTypesFromDatabase()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, eventTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedEventType = parent.getItemAtPosition(pos) as Int
                CoroutineScope(Dispatchers.IO).launch {
                    loadHeatmap(selectedEventType)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
    }

    private fun getEventTypesFromDatabase(): List<Int> {
        return listOf(1, 2, 3, 4, 5, 6, 7, 8, 9) // Reemplaza esto con tu código real para obtener los datos de la base de datos
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            loadHeatmap(selectedEventType)
        }
    }

    private suspend fun loadHeatmap(eventType: Int) {
        withContext(Dispatchers.IO) {
            val url = "http://54.227.125.166/barrios_inteligentes/assets/php/heatmap.php"
            val request = Request.Builder().url(url).build()
            val response = OkHttpClient().newCall(request).execute()
            val jsonArray = JSONArray(response.body?.string())
            val list = ArrayList<LatLng>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getInt("tipo_denuncia") == eventType) {
                    list.add(LatLng(obj.getDouble("latitud"), obj.getDouble("longitud")))
                }
            }

            if(list.isNotEmpty()) {
                val provider = HeatmapTileProvider.Builder().data(list).build()
                withContext(Dispatchers.Main) {
                    lastHeatmapOverlay?.remove() // Remove the last overlay if it exists
                    lastHeatmapOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(provider)) // Save the reference to the new overlay
                }
            } else {
                Log.d("HeatmapData", "No data for heatmap")
            }
        }
    }
}

