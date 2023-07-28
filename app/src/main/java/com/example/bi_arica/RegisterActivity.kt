package com.example.bi_arica

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bi_arica.databinding.ActivityRegisterBinding
import java.text.SimpleDateFormat
import java.util.*

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
                // Your code to send data to your PHP API
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
        if (rut.length >= 8) {
            rut = rut.substring(0, rut.length - 1) + "-" + rut.substring(rut.length - 1)
            for (i in 4 downTo 1) {
                if (rut.length > i + 2) {
                    rut = rut.substring(0, rut.length - (i * 2 + 1)) + "." + rut.substring(rut.length - (i * 2 + 1))
                }
            }

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
}

