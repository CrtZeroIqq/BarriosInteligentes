package com.example.bi_arica

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bi_arica.databinding.ActivityRegisterBinding
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var calendar: Calendar = Calendar.getInstance()

    private val rutWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            formatRUT(binding.rut)
            validateRUT(binding.rut.text.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        binding.birthDate.setOnClickListener { showDatePickerDialog() }

        // Set listener to format and validate RUT
        binding.rut.addTextChangedListener(rutWatcher)

        binding.registerButton.setOnClickListener {
            if (validateFields()) {
                registerUser()
            }
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(selectedDate.time)
                binding.birthDate.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.show()
    }

    private fun formatRUT(rutField: EditText) {
        var rut = rutField.text.toString().replace("[.-]".toRegex(), "")
        if (rut.length >= 2) {
            rut = rut.substring(0, rut.length - 1) + "-" + rut.substring(rut.length - 1)

            rutField.removeTextChangedListener(rutWatcher) //remove watcher
            rutField.setText(rut)
            rutField.addTextChangedListener(rutWatcher) //add watcher again
            rutField.setSelection(rutField.text.toString().length) //move cursor to end
        }
    }

    private fun validateRUT(rut: String): Boolean {
        var valid = false
        // Remove the format from RUT and split it into number and DV
        val rutSplit = rut.replace("\\.", "").split("-").toTypedArray()

        if (rutSplit.size == 2) {
            try {
                val number = rutSplit[0].toInt()
                val dv = rutSplit[1].toUpperCase(Locale.ROOT)

                var total = 0
                var factor = 2

                for (i in number.toString().length - 1 downTo 0) {
                    total += Character.getNumericValue(number.toString()[i]) * factor
                    factor = if (factor == 7) 2 else factor + 1
                }

                val mod = total % 11
                val ver = 11 - mod

                if ((ver == 11 && dv == "0") || (ver == 10 && dv == "K") || ver.toString() == dv) {
                    valid = true
                }
            } catch (e: NumberFormatException) {
                // La cadena RUT no se pudo convertir a un entero
                valid = false
            }
        }

        binding.rut.error = if (!valid) "RUT inválido" else null
        binding.rut.setTextColor(if (valid) Color.BLACK else Color.RED)

        return valid
    }

    private fun validateFields(): Boolean {
        var isValid = true

        // Validate RUT
        isValid = validateRUT(binding.rut.text.toString())

        // Validate other fields
        // TODO: Add other field validations here

        return isValid
    }

    private fun registerUser() {
        val url = "http://54.227.125.166/barrios_inteligentes/assets/php/register.php"

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("first_name", binding.firstName.text.toString())
            .add("last_name", binding.lastName.text.toString())
            .add("rut", binding.rut.text.toString())
            .add("birth_date", binding.birthDate.text.toString())
            .add("email", binding.email.text.toString())
            .add("phone_number", binding.phoneNumber.text.toString())
            .add("neighborhood", binding.neighborhood.text.toString())
            .add("password", binding.password.text.toString())
            .add("password_confirmation", binding.passwordConfirmation.text.toString())
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseStr = response.body?.string()

                    // update UI on the main thread
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "User registered successfully!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = response.message ?: "Unknown error"
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }


}


