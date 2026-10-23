package com.example.ep2_

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var etCorreo : EditText
    private lateinit var etpass : EditText
    private lateinit var btnInicio : Button
    private lateinit var dbHelper : DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)

        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, Registrar::class.java)
            startActivity(intent)
        }

        etCorreo = findViewById(R.id.etCorreo)
        etpass = findViewById(R.id.etPassword)
        btnInicio = findViewById(R.id.btnIngresar)
        dbHelper = DatabaseHelper(this)

        // Evento para Iniciar Sesion
        btnInicio.setOnClickListener {
            // Todo: Agregar Metodo de Inicio de Sesion
            iniciarSesion()
        }
    }
    // Metodo para iniciar Sesion
    private fun  iniciarSesion() {

        // Acceder a la informacion que escribe el usuario
        val correo = etCorreo.text.toString().trim()
        val pass = etpass.text.toString().trim()

        if (correo.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Llamar al metodo de inicio de sesion en la BD
        val resultado = dbHelper.iniciarSesion(correo, pass)

        if(resultado){
            // Si la sesion es correcta
            Toast.makeText(this,"Bienvenido :D", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            // Cerrar la Pantalla Actual
            finish()
        }else {
            Toast.makeText(this,"Datos Incorrectos :(", Toast.LENGTH_SHORT).show()
        }

    }
}