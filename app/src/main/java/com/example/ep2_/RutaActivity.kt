package com.example.ep2_

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
    private lateinit var tvResultado: TextView
    private lateinit var imgDestino: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ruta)

        etDestino = findViewById(R.id.etDestino)
        btnBuscar = findViewById(R.id.btnBuscar)
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

                    // 1. Mostrar información textual
                    tvResultado.text = "Lugar Encontrado: ${lugar.obtenerNombreCorto()}\n\n" +
                            "Dirección Completa:\n${lugar.display_name}\n\n" +
                            "Coordenadas:\n${lugar.obtenerCoordenadas()}\n\n" +
                            "Categoría: ${lugar.categoria ?: "N/A"}"

                    // 2. Convertir coordenadas
                    val lat = lugar.lat?.toDoubleOrNull() ?: 0.0
                    val lon = lugar.lon?.toDoubleOrNull() ?: 0.0

                    // 3. Obtener URL del mapa de OpenStreetMap y cargarlo con Glide
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

                    // 4. Configurar el evento de clic en la imagen del mapa para abrir Google Maps
                    imgDestino.setOnClickListener {
                        abrirEnGoogleMaps(lat, lon, lugar.obtenerNombreCorto())
                    }

                } else {
                    Toast.makeText(this@RutaActivity, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Lugar>>, t: Throwable) {
                Toast.makeText(this@RutaActivity, "Error en el API: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Metodo para abrir las coordenadas en Google Maps
    private fun abrirEnGoogleMaps(lat: Double, lon: Double, nombreLugar: String) {
        val uriMap = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(nombreLugar)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, uriMap)
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            // Si la aplicación de Google Maps no está disponible, lo abre en el navegador web
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