package com.example.ep2_

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ep2_.model.Lugar
import com.example.ep2_.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class RutaActivity : AppCompatActivity() {

    private lateinit var etDestino: EditText
    private lateinit var btnBuscar: Button
    private lateinit var tvResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ruta)

        etDestino = findViewById(R.id.etDestino)
        btnBuscar = findViewById(R.id.btnBuscar)
        tvResultado = findViewById(R.id.tvResultado)

        btnBuscar.setOnClickListener {
            val destino = etDestino.text.toString().trim()
            if (destino.isNotEmpty()) {
                obtenerUbicacionDestino(destino)
            } else {
                Toast.makeText(this, "Ingresa un lugar de destino", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Metodo para consultar la ubicación en OpenStreetMap
    private fun obtenerUbicacionDestino(destino: String) {

        RetrofitClient.api.buscarLugar(
            consulta = destino,
            formato = "json",
            limite = 1
        ).enqueue(object : Callback<List<Lugar>> {

            // Metodo para verificar que la respuesta sea correcta
            override fun onResponse(call: Call<List<Lugar>>, response: Response<List<Lugar>>) {

                // Variable que obtiene los resultados del API
                val lugares = response.body()

                if (!lugares.isNullOrEmpty()) {

                    // Variable que guarda la información de la respuesta obtenida
                    val lugar = lugares[0]

                    tvResultado.text = "Lugar Encontrado: ${lugar.obtenerNombreCorto()}\n\n" +
                            "Dirección Completa:\n${lugar.display_name}\n\n" +
                            "Coordenadas:\n${lugar.obtenerCoordenadas()}\n\n" +
                            "Categoría: ${lugar.categoria ?: "N/A"}"

                } else {
                    Toast.makeText(this@RutaActivity, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                }
            }

            // Metodo para verificar si no hay respuesta o falla de red
            override fun onFailure(call: Call<List<Lugar>>, t: Throwable) {
                Toast.makeText(this@RutaActivity, "Error en el API: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}