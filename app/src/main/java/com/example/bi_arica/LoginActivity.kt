package com.example.bi_arica

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bi_arica.databinding.ActivityLoginBinding
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.username.text.toString()
            val password = binding.password.text.toString()
            validateLogin(email, password)
        }

        binding.registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateLogin(email: String, password: String) {
        val url = "http://54.227.125.166/barrios_inteligentes/assets/php/login.php"

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Error de conexión", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseStr = response.body?.string()
                    println("DEBUG: Response from server: $responseStr") // Línea de depuración añadida

                    val jsonObject = JSONObject(responseStr)

                    val id = jsonObject.getInt("id")
                    val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                    with (sharedPref.edit()) {
                        putInt("UserId", id)
                        commit()
                    }

                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Ingreso exitoso", Toast.LENGTH_LONG).show()
                        startDashboardActivity()
                    }
                } else {
                    val errorMessage = response.message ?: "Error desconocido"
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun startDashboardActivity() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}