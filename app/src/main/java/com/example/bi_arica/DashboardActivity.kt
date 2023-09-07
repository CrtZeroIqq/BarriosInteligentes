package com.example.bi_arica

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bi_arica.databinding.ActivityDashboardBinding
import com.google.android.gms.location.*
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageButton
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import android.widget.Button
import android.content.Intent
import com.google.android.gms.location.LocationCallback


class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val PERMISSION_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
        val heatmapButton: Button = findViewById(R.id.heatmap)
        heatmapButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }


        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar la solicitud de ubicación
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Crear el callback de ubicación
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation
            }
        }

        // Comenzar las actualizaciones de ubicación
        startLocationUpdates(locationRequest)

        // Obtener el ID del usuario desde SharedPreferences
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("UserId", -1)

        // Configurar los botones para que al hacer clic en ellos se envíe la ubicación del usuario al servidor
        for (id in arrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9)) {
            findViewById<ImageButton>(id).setOnClickListener { v ->
                val tag = v.tag.toString()
                AlertDialog.Builder(this)
                    .setTitle("Confirmación")
                    .setMessage("¿Estás seguro de que quieres enviar este reporte?")
                    .setPositiveButton("Sí") { dialog, which ->
                        getLastLocationAndSendToServer(tag, userId)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso concedido
                } else {
                    // Permiso denegado
                    Toast.makeText(this, "Permiso de ubicación necesario para enviar el reporte", Toast.LENGTH_LONG).show()
                }
                return
            }
            else -> {
                // Ignorar todos los otros casos de resultados de solicitud
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun getLastLocationAndSendToServer(tag: String, userId: Int) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                sendReportToServer(tag, userId, location)
            }
        }
    }

    private fun sendReportToServer(tag: String, userId: Int, location: Location) {
        val url = "http://54.227.125.166/barrios_inteligentes/assets/php/denuncia.php".toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("user", userId.toString())
            .addQueryParameter("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
            .addQueryParameter("type", tag)
            .addQueryParameter("lat", location.latitude.toString())
            .addQueryParameter("long", location.longitude.toString())
            .build()

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@DashboardActivity, "Error de conexión", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@DashboardActivity, "Reporte enviado exitosamente", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = response.message ?: "Error desconocido"
                    runOnUiThread {
                        Toast.makeText(this@DashboardActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
