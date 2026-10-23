package com.example.ep2_

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Registrar : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText

    private lateinit var btnRegistrar: Button

    // Crear objeto de la clase de la base de datos
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar)


        etNombre = findViewById(R.id.etNombre)
        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnRegistrar = findViewById(R.id.btnRegistrar)

        // Iniciar el objeto de la base de datos
        dbHelper = DatabaseHelper(this)

        // Evento para registrar a un usuario

        btnRegistrar.setOnClickListener {
            // Llamar al metodo de registrar usuario
            registrarUsuario()
        }
    }

    // Metodo para Registrar a un Usuario
    private fun registrarUsuario() {

        // Obtener la informacion que escribe el usuario
        val nombre = etNombre.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validar que los campos tengan informacion
        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Llamar a nuestra base de datos y guardar el resultado de la insercion
        val resultado = dbHelper.registrarUsuario(nombre, correo, password)

        if (resultado) {
            // Si el resultado es verdadero
            Toast.makeText(this, "Registro Exitoso.!", Toast.LENGTH_SHORT).show()
            // Regresar al usuario a la pantalla anterior
            finish()
        } else {
            Toast.makeText(this, "Registro Fallido.", Toast.LENGTH_SHORT).show()

        }
    }
}