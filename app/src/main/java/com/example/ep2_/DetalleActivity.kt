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
    }

    // Metodo para obtener toda la informacion y gestionar favoritos
    private fun mostrarInformacionLugar() {

        val placeId = intent.getLongExtra("EXTRA_PLACE_ID", -1L)
        val nombre = intent.getStringExtra("EXTRA_NOMBRE") ?: "Lugar Desconocido"
        val direccion = intent.getStringExtra("EXTRA_DIRECCION") ?: "Sin Dirección"
        val lat = intent.getStringExtra("EXTRA_LAT") ?: "0.0"
        val lon = intent.getStringExtra("EXTRA_LON") ?: "0.0"
        val categoria = intent.getStringExtra("EXTRA_CATEGORIA") ?: "N/A"

        // Asignar valores a la vista
        tvNombreDetalle.text = nombre
        tvDireccionDetalle.text = direccion
        tvCoordenadasDetalle.text = "Lat: $lat, Lon: $lon"
        tvCategoriaDetalle.text = categoria

        // Validacion inicial para verificar si el lugar ya existe en favoritos
        if (placeId != -1L) {
            lugarFavorito = dbHelper.existeLugarFavorito(placeId)
            actualizarInterfazFavorito()
        }

        // Funcion reutilizable para guardar o eliminar al presionar cualquiera de los 2 botones
        val alternarFavorito = {
            if (lugarFavorito) {
                val eliminado = dbHelper.eliminarLugarFavorito(placeId)

                if (eliminado) {
                    lugarFavorito = !lugarFavorito
                    actualizarInterfazFavorito()
                    Toast.makeText(this, "$nombre eliminado de favoritos", Toast.LENGTH_SHORT).show()
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
                    lugarFavorito = !lugarFavorito
                    actualizarInterfazFavorito()
                    Toast.makeText(this, "$nombre agregado a favoritos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnFavorito.setOnClickListener { alternarFavorito() }
        btnGuardarBD.setOnClickListener { alternarFavorito() }
    }

    // Actualiza el icono del corazon y el texto del boton segun el estado en la BD
    private fun actualizarInterfazFavorito() {
        if (lugarFavorito) {
            btnFavorito.setImageResource(R.drawable.favorito)
            btnGuardarBD.text = "ELIMINAR DE FAVORITOS"
        } else {
            btnFavorito.setImageResource(R.drawable.no_favorite)
            btnGuardarBD.text = "GUARDAR EN FAVORITOS"
        }
    }
}