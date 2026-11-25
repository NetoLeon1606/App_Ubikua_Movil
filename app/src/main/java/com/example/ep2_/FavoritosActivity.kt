package com.example.ep2_

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.ep2_.model.LugarFavorito
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.tan

class FavoritosActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    private lateinit var gridFavoritos: GridLayout
    private lateinit var tvEstado: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favoritos)

        bottomNavigationView = findViewById(R.id.bottomNavigation)
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        gridFavoritos = findViewById(R.id.gridFavoritos)
        tvEstado = findViewById(R.id.tvEstado)
        dbHelper = DatabaseHelper(this)

        menuNavegacion()
        consultarFavoritos()
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.nav_favoritos
        consultarFavoritos()
    }

    private fun menuNavegacion() {
        bottomNavigationView.selectedItemId = R.id.nav_favoritos

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, RutaActivity::class.java))
                    true
                }
                R.id.nav_favoritos -> true
                R.id.nav_salir -> {
                    cerrarSesionGoogle()
                    true
                }
                else -> false
            }
        }
    }

    private fun cerrarSesionGoogle() {
        auth.signOut()

        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Toast.makeText(this@FavoritosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this@FavoritosActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun mostrarLugaresFavoritos(favoritos: List<LugarFavorito>) {
        for (lugar in favoritos) {
            val vistaLugar = layoutInflater.inflate(
                R.layout.item_favorito,
                gridFavoritos,
                false
            )

            val imgMapa = vistaLugar.findViewById<ImageView>(R.id.imgFavoritoMapa)
            val tvNombre = vistaLugar.findViewById<TextView>(R.id.tvFavoritoNombre)
            val tvCategoria = vistaLugar.findViewById<TextView>(R.id.tvFavoritoCategoria)

            tvNombre.text = lugar.nombreLugar
            tvCategoria.text = lugar.categoria

            val lat = lugar.latitud.toDoubleOrNull() ?: 0.0
            val lon = lugar.longitud.toDoubleOrNull() ?: 0.0
            val glideUrl = GlideUrl(
                obtenerUrlTileOSM(lat, lon, zoom = 15),
                LazyHeaders.Builder()
                    .addHeader("User-Agent", "EP2_Limpio_Ubikua/1.0")
                    .build()
            )

            Glide.with(this)
                .load(glideUrl)
                .placeholder(R.drawable.expopantalla)
                .error(R.drawable.expopantalla)
                .centerCrop()
                .into(imgMapa)

            vistaLugar.setOnClickListener {
                val intent = Intent(this, DetalleActivity::class.java).apply {
                    putExtra("EXTRA_PLACE_ID", lugar.placeId)
                    putExtra("EXTRA_NOMBRE", lugar.nombreLugar)
                    putExtra("EXTRA_DIRECCION", lugar.direccion)
                    putExtra("EXTRA_LAT", lugar.latitud)
                    putExtra("EXTRA_LON", lugar.longitud)
                    putExtra("EXTRA_CATEGORIA", lugar.categoria)
                }
                startActivity(intent)
            }

            gridFavoritos.addView(vistaLugar)
        }
    }

    private fun consultarFavoritos() {
        val favoritos = dbHelper.obtenerLugaresFavoritos()

        gridFavoritos.removeAllViews()

        if (favoritos.isEmpty()) {
            tvEstado.text = "No tienes ningún lugar guardado en favoritos :("
            tvEstado.visibility = View.VISIBLE
        } else {
            tvEstado.visibility = View.GONE
            mostrarLugaresFavoritos(favoritos)
        }
    }

    private fun obtenerUrlTileOSM(lat: Double, lon: Double, zoom: Int): String {
        val n = 1 shl zoom
        val x = ((lon + 180.0) / 360.0 * n).toInt()
        val latRad = Math.toRadians(lat)
        val y = ((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n).toInt()
        return "https://tile.openstreetmap.org/$zoom/$x/$y.png"
    }
}