package com.example.ep2_

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritosActivity : AppCompatActivity() {

    private lateinit var rvFavoritos: RecyclerView
    private lateinit var tvSinFavoritos: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: FavoritosAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.item_favorito)

        dbHelper = DatabaseHelper(this)
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        rvFavoritos = findViewById(R.id.rvFavoritos)
        tvSinFavoritos = findViewById(R.id.tvSinFavoritos)
        bottomNavigationView = findViewById(R.id.bottomNavigation)

        rvFavoritos.layoutManager = LinearLayoutManager(this)

        adapter = FavoritosAdapter(
            listaLugares = emptyList(),
            alHacerClick = { lugar ->
                val intent = Intent(this, DetalleActivity::class.java).apply {
                    putExtra("EXTRA_PLACE_ID", lugar.placeId)
                    putExtra("EXTRA_NOMBRE", lugar.nombreLugar)
                    putExtra("EXTRA_DIRECCION", lugar.direccion)
                    putExtra("EXTRA_LAT", lugar.latitud)
                    putExtra("EXTRA_LON", lugar.longitud)
                    putExtra("EXTRA_CATEGORIA", lugar.categoria)
                }
                startActivity(intent)
            },
            alEliminarClick = { lugar ->
                val eliminado = dbHelper.eliminarLugarFavorito(lugar.placeId)
                if (eliminado) {
                    Toast.makeText(this, "${lugar.nombreLugar} eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    cargarFavoritos()
                }
            }
        )

        rvFavoritos.adapter = adapter
        menuNavegacion()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.nav_favoritos
        cargarFavoritos()
    }

    private fun cargarFavoritos() {
        val favoritos = dbHelper.obtenerLugaresFavoritos()
        adapter.actualizarLista(favoritos)

        if (favoritos.isEmpty()) {
            tvSinFavoritos.visibility = View.VISIBLE
            rvFavoritos.visibility = View.GONE
        } else {
            tvSinFavoritos.visibility = View.GONE
            rvFavoritos.visibility = View.VISIBLE
        }
    }

    private fun menuNavegacion() {
        bottomNavigationView.selectedItemId = R.id.nav_favoritos

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    finish()
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
                credentialManager.clearCredentialState(
                    androidx.credentials.ClearCredentialStateRequest()
                )
            } catch (e: Exception) {
                Toast.makeText(this@FavoritosActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(this@FavoritosActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}