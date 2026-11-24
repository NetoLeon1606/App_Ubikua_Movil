package com.example.ep2_

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.ep2_.model.LugarFavorito

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    companion object {
        private const val DATABASE_NAME = "ubikua.db" // Mantén el nombre que ya uses en tu proyecto
        private const val DATABASE_VERSION = 2

        // Tabla de Usuarios
        private const val TABLE_USUARIOS = "usuarios"
        private const val COL_ID = "id"
        private const val COL_NOMBRE = "nombre"
        private const val COL_CORREO = "correo"
        const val COL_PASSWORD = "password"

        // Nueva tabla de Lugares Favoritos (API OpenStreetMap)
        const val TABLE_FAVORITOS = "favoritos"
        const val COL_FAVORITO = "id"
        const val COL_PLACE_ID = "place_id"
        const val COL_NOMBRE_LUGAR = "nombre_lugar"
        const val COL_DIRECCION = "direccion"
        const val COL_LATITUD = "latitud"
        const val COL_LONGITUD = "longitud"
        const val COL_CATEGORIA = "categoria"

        // Crear tabla de usuarios
        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE $TABLE_USUARIOS(
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_NOMBRE TEXT NOT NULL,
            $COL_CORREO TEXT NOT NULL UNIQUE,
            $COL_PASSWORD TEXT NOT NULL
            )
        """

        // Crear tabla de favoritos
        private const val CREATE_TABLE_FAVORITOS = """
            CREATE TABLE $TABLE_FAVORITOS(
            $COL_FAVORITO INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_PLACE_ID INTEGER UNIQUE NOT NULL,
            $COL_NOMBRE_LUGAR TEXT NOT NULL,
            $COL_DIRECCION TEXT,
            $COL_LATITUD TEXT,
            $COL_LONGITUD TEXT,
            $COL_CATEGORIA TEXT
            )
        """
    }

    // Metodo para crear la base de datos
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_USUARIOS)
        db?.execSQL(CREATE_TABLE_FAVORITOS)
    }

    // Metodo para actualizar la base de datos
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, nextVersion: Int) {
        // Si la version anterior es menor a 2, crea la nueva tabla de favoritos
        if (oldVersion < 2) {
            db?.execSQL(CREATE_TABLE_FAVORITOS)
        }
    }

    // Metodo para Insertar Registro de Usuario
    fun registrarUsuario(nombre: String, correo: String, password: String): Boolean {
        val db = writableDatabase

        val datos = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, password)
        }

        val resultado = db.insert(TABLE_USUARIOS, null, datos)
        db.close()

        return resultado != -1L
    }

    // Metodo para Consultar a un Usuario
    fun iniciarSesion(correo: String, password: String): Boolean {
        val db = readableDatabase

        val query_sql = """
            SELECT * FROM $TABLE_USUARIOS 
            WHERE $COL_CORREO = ? AND $COL_PASSWORD = ?
        """.trimIndent()

        val cursor = db.rawQuery(
            query_sql, arrayOf(correo, password)
        )

        val usuarioEncontrardo = cursor.count > 0

        cursor.close()
        db.close()

        return usuarioEncontrardo
    }

    // 1. Metodo para registrar lugar proveniente del API en favoritos
    fun guardarLugarFavorito(
        placeId: Long,
        nombreLugar: String,
        direccion: String,
        latitud: String,
        longitud: String,
        categoria: String
    ): Boolean {
        val db = writableDatabase

        val datos = ContentValues().apply {
            put(COL_PLACE_ID, placeId)
            put(COL_NOMBRE_LUGAR, nombreLugar)
            put(COL_DIRECCION, direccion)
            put(COL_LATITUD, latitud)
            put(COL_LONGITUD, longitud)
            put(COL_CATEGORIA, categoria)
        }

        val resultado = db.insertWithOnConflict(
            TABLE_FAVORITOS,
            null,
            datos,
            SQLiteDatabase.CONFLICT_IGNORE
        )

        db.close()

        return resultado != -1L
    }

    // 2. Metodo para eliminar un lugar de favoritos
    fun eliminarLugarFavorito(placeId: Long): Boolean {
        val db = writableDatabase

        val filas_afectadas = db.delete(
            TABLE_FAVORITOS,
            "$COL_PLACE_ID = ?",
            arrayOf(placeId.toString())
        )

        db.close()

        return filas_afectadas > 0
    }

    // 3. Metodo para verificar si un lugar ya existe en favoritos
    fun existeLugarFavorito(placeId: Long): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_FAVORITO FROM $TABLE_FAVORITOS WHERE $COL_PLACE_ID = ?",
            arrayOf(placeId.toString())
        )

        val existe = cursor.moveToFirst()
        cursor.close()
        db.close()

        return existe
    }

    fun obtenerLugaresFavoritos(): List<LugarFavorito> {
        val lista = mutableListOf<LugarFavorito>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM favoritos", null)

        if (cursor.moveToFirst()) {
            do {
                val lugar = LugarFavorito(
                    placeId = cursor.getLong(cursor.getColumnIndexOrThrow("place_id")),
                    nombreLugar = cursor.getString(cursor.getColumnIndexOrThrow("nombre_lugar")),
                    direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion")) ?: "Sin dirección",
                    latitud = cursor.getString(cursor.getColumnIndexOrThrow("latitud")) ?: "0.0",
                    longitud = cursor.getString(cursor.getColumnIndexOrThrow("longitud")) ?: "0.0",
                    categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria")) ?: "N/A"
                )
                lista.add(lugar)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return lista
    }
}