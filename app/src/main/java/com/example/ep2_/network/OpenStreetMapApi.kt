package com.example.ep2_.network


import com.example.ep2_.model.Lugar
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
// Archivo que permite generar la petición al API de OpenStreetMap

interface OpenStreetMapApi {

    @Headers("User-Agent: EP2_Limpio_Ubikua/1.0")
    @GET("search")

    // Método para buscar ubicaciones por nombre de destino o partida
    fun buscarLugar(
        @Query("q") consulta: String,
        @Query("format") formato: String = "json",
        @Query("limit") limite: Int = 1
    ): Call<List<Lugar>>
}