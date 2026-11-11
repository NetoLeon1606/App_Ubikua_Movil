package com.example.ep2_

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class DetalleActivity : AppCompatActivity() {

    private lateinit var tvNombreDetalle: TextView
    private lateinit var tvDireccionDetalle: TextView
    private lateinit var tvCoordenadasDetalle: TextView
    private lateinit var tvCategoriaDetalle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle)

        tvNombreDetalle = findViewById(R.id.tvNombreDetalle)
        tvDireccionDetalle = findViewById(R.id.tvDireccionDetalle)
        tvCoordenadasDetalle = findViewById(R.id.tvCoordenadasDetalle)
        tvCategoriaDetalle = findViewById(R.id.tvCategoriaDetalle)

        // Obtener extras pasados en el Intent
        val nombre = intent.getStringExtra("EXTRA_NOMBRE") ?: "Sin Nombre"
        val direccion = intent.getStringExtra("EXTRA_DIRECCION") ?: "Sin Dirección"
        val lat = intent.getStringExtra("EXTRA_LAT") ?: "0.0"
        val lon = intent.getStringExtra("EXTRA_LON") ?: "0.0"
        val categoria = intent.getStringExtra("EXTRA_CATEGORIA") ?: "N/A"

        // Desplegar datos recibidos
        tvNombreDetalle.text = nombre
        tvDireccionDetalle.text = direccion
        tvCoordenadasDetalle.text = "Lat: $lat, Lon: $lon"
        tvCategoriaDetalle.text = categoria

        val btnFavorito = findViewById<ImageButton>(R.id.btnFavorito)

        btnFavorito.setOnClickListener {
            Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
        }
    }
}