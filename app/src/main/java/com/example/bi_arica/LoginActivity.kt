package com.example.bi_arica
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bi_arica.databinding.ActivityLoginBinding

class    LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            // Tu código para validar el inicio de sesión va aquí
            // Luego si el inicio de sesión es exitoso, inicia RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.registerLink.setOnClickListener {
            // Iniciar la actividad de registro.
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
