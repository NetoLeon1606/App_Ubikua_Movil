package com.example.ep2_.model

import com.google.gson.annotations.SerializedName

data class Lugar(
    val place_id: Long?,
    val licence: String?,
    val osm_type: String?,
    val osm_id: Long?,
    val lat: String?,
    val lon: String?,
    val display_name: String?,

    @SerializedName("class")
    val categoria: String?,

    val type: String?,
    val importance: Double?
) {
    fun obtenerCoordenadas(): String {
        return "Lat: ${lat ?: "0.0"}, Lon: ${lon ?: "0.0"}"
    }

    fun obtenerNombreCorto(): String {
        return display_name?.split(",")?.firstOrNull() ?: "Ubicación sin nombre"
    }
}