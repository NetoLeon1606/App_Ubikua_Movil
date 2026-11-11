package com.example.ep2_

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.ep2_.model.Lugar
import com.example.ep2_.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.tan

class RutaActivity : AppCompatActivity() {

    private lateinit var etDestino: EditText
    private lateinit var btnBuscar: Button
    private lateinit var btnVerMas: Button
    private lateinit var tvResultado: TextView
    private lateinit var imgDestino: ImageView

    private var lugarActual: Lugar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ruta)

        etDestino = findViewById(R.id.etDestino)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnVerMas = findViewById(R.id.btnVerMas)
        tvResultado = findViewById(R.id.tvResultado)
        imgDestino = findViewById(R.id.imgDestino)

        btnBuscar.setOnClickListener {
            val destino = etDestino.text.toString().trim()
            if (destino.isNotEmpty()) {
                obtenerUbicacionDestino(destino)
            } else {
                Toast.makeText(this, "Ingresa un lugar de destino", Toast.LENGTH_SHORT).show()
            }
        }

        // Al hacer clic, enviamos toda la información a DetalleActivity
        btnVerMas.setOnClickListener {
            lugarActual?.let { lugar ->
                val intent = Intent(this, DetalleActivity::class.java).apply {
                    putExtra("EXTRA_NOMBRE", lugar.obtenerNombreCorto())
                    putExtra("EXTRA_DIRECCION", lugar.display_name ?: "Sin dirección")
                    putExtra("EXTRA_LAT", lugar.lat ?: "0.0")
                    putExtra("EXTRA_LON", lugar.lon ?: "0.0")
                    putExtra("EXTRA_CATEGORIA", lugar.categoria ?: "N/A")
                }
                startActivity(intent)
            }
        }
    }

    private fun obtenerUbicacionDestino(destino: String) {
        RetrofitClient.api.buscarLugar(
            consulta = destino,
            formato = "json",
            limite = 1
        ).enqueue(object : Callback<List<Lugar>> {

            override fun onResponse(call: Call<List<Lugar>>, response: Response<List<Lugar>>) {
                val lugares = response.body()

                if (!lugares.isNullOrEmpty()) {
                    val lugar = lugares[0]
                    lugarActual = lugar

                    // 1. Mostrar SOLO el nombre del lugar encontrado
                    tvResultado.text = "Lugar Encontrado: ${lugar.obtenerNombreCorto()}"

                    // 2. Hacer visible únicamente el botón "VER MÁS DETALLES"
                    btnVerMas.visibility = View.VISIBLE

                    // 3. Cargar el tile del mapa con Glide
                    val lat = lugar.lat?.toDoubleOrNull() ?: 0.0
                    val lon = lugar.lon?.toDoubleOrNull() ?: 0.0

                    val urlTileOSM = obtenerUrlTileOSM(lat, lon, zoom = 16)
                    val glideUrl = GlideUrl(
                        urlTileOSM,
                        LazyHeaders.Builder()
                            .addHeader("User-Agent", "EP2_Limpio_Ubikua/1.0")
                            .build()
                    )

                    Glide.with(this@RutaActivity)
                        .load(glideUrl)
                        .placeholder(R.drawable.expopantalla)
                        .error(R.drawable.expopantalla)
                        .into(imgDestino)

                    imgDestino.setOnClickListener {
                        abrirEnGoogleMaps(lat, lon, lugar.obtenerNombreCorto())
                    }

                } else {
                    btnVerMas.visibility = View.GONE
                    tvResultado.text = "No se encontraron resultados"
                    Toast.makeText(this@RutaActivity, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Lugar>>, t: Throwable) {
                btnVerMas.visibility = View.GONE
                tvResultado.text = "Error al conectar con la API"
                Toast.makeText(this@RutaActivity, "Error en el API: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun abrirEnGoogleMaps(lat: Double, lon: Double, nombreLugar: String) {
        val uriMap = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(nombreLugar)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, uriMap)
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            startActivity(webIntent)
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