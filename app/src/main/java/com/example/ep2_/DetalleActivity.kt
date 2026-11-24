package com.example.ep2_

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class DetalleActivity : AppCompatActivity() {

    private lateinit var tvNombreDetalle: TextView
    private lateinit var tvDireccionDetalle: TextView
    private lateinit var tvCoordenadasDetalle: TextView
    private lateinit var tvCategoriaDetalle: TextView
    private lateinit var btnGuardarBD: Button
    private lateinit var btnFavorito: ImageButton

    private lateinit var dbHelper: DatabaseHelper
    private var lugarFavorito = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle)

        dbHelper = DatabaseHelper(this)

        tvNombreDetalle = findViewById(R.id.tvNombreDetalle)
        tvDireccionDetalle = findViewById(R.id.tvDireccionDetalle)
        tvCoordenadasDetalle = findViewById(R.id.tvCoordenadasDetalle)
        tvCategoriaDetalle = findViewById(R.id.tvCategoriaDetalle)
        btnGuardarBD = findViewById(R.id.btnGuardarBD)
        btnFavorito = findViewById(R.id.btnFavorito)

        mostrarInformacionLugar()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun mostrarInformacionLugar() {
        val rawPlaceId = intent.getLongExtra("EXTRA_PLACE_ID", -1L)
        val nombre = intent.getStringExtra("EXTRA_NOMBRE") ?: "Lugar Desconocido"
        val direccion = intent.getStringExtra("EXTRA_DIRECCION") ?: "Sin Dirección"
        val lat = intent.getStringExtra("EXTRA_LAT") ?: "0.0"
        val lon = intent.getStringExtra("EXTRA_LON") ?: "0.0"
        val categoria = intent.getStringExtra("EXTRA_CATEGORIA") ?: "N/A"

        // Si el API no devuelve place_id, se genera un ID único basado en sus datos
        val placeId = if (rawPlaceId != -1L) {
            rawPlaceId
        } else {
            "$nombre$lat$lon".hashCode().toLong()
        }

        // Mostrar datos en pantalla
        tvNombreDetalle.text = nombre
        tvDireccionDetalle.text = direccion
        tvCoordenadasDetalle.text = "Lat: $lat, Lon: $lon"
        tvCategoriaDetalle.text = categoria

        // 1. Verificar en SQLite si el lugar ya estaba guardado previamente
        lugarFavorito = dbHelper.existeLugarFavorito(placeId)
        actualizarEstadoBoton()

        // 2. Acción de guardar o eliminar al presionar el botón
        val gestionarFavorito = {
            if (lugarFavorito) {
                val eliminado = dbHelper.eliminarLugarFavorito(placeId)
                if (eliminado) {
                    lugarFavorito = false
                    actualizarEstadoBoton()
                    Toast.makeText(this, "$nombre eliminado de favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se pudo eliminar de favoritos", Toast.LENGTH_SHORT).show()
                }
            } else {
                val guardado = dbHelper.guardarLugarFavorito(
                    placeId = placeId,
                    nombreLugar = nombre,
                    direccion = direccion,
                    latitud = lat,
                    longitud = lon,
                    categoria = categoria
                )
                if (guardado) {
                    lugarFavorito = true
                    actualizarEstadoBoton()
                    Toast.makeText(this, "$nombre guardado en favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al guardar en favoritos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnFavorito.setOnClickListener { gestionarFavorito() }
        btnGuardarBD.setOnClickListener { gestionarFavorito() }
    }

    private fun actualizarEstadoBoton() {
        if (lugarFavorito) {
            btnFavorito.setImageResource(R.drawable.favorito)
            btnGuardarBD.text = "ELIMINAR DE FAVORITOS"
        } else {
            btnFavorito.setImageResource(R.drawable.no_favorite)
            btnGuardarBD.text = "GUARDAR EN FAVORITOS"
        }
    }
}