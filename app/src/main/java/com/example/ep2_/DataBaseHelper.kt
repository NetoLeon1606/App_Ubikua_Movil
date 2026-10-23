package com.example.ep2_

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION,
) {

    companion object {
        private const val DATABASE_NAME = "ubikua.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USUARIOS = "usuarios"
        private const val COL_ID = "id"
        private const val COL_NOMBRE = "nombre"


        private const val COL_CORREO = "correo"
        private const val COL_PASSWORD = "password"

    }

    // Metodo para crear la base de datos
    override fun onCreate(db: SQLiteDatabase) {
        val crearTablaUsuarios = """
            CREATE TABLE $TABLE_USUARIOS(
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE TEXT NOT NULL,
                $COL_CORREO TEXT NOT NULL UNIQUE,
                $COL_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()
        // validacion ? cumple : ejecutar codigo

        // Crear tabla de SQL  Usamos db? en lugar de p0? para que coincida con el parámetro de la función
        db.execSQL(crearTablaUsuarios)
    }

    // Metodo para actualizar la base de datos
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, nextVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }

    // Metodo para Insertar Registro
    fun registrarUsuario(nombre: String, correo: String, password: String): Boolean {

        // Crear variable para que la base de datos se pueda modificar
        val db = writableDatabase

        // Crear variable para recibir los datos que escribe el usuario
        val datos = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, password)
        }

        // Insertar datos en la tabla
        val resultado = db.insert(TABLE_USUARIOS, null, datos)

        // Cerrar conexion con la BD
        db.close()

        // 1L o -1L regresa esos datos + fue exitosa - no fue exitosa
        // Comparar el valor del resultado de la insercion y regresarlo como Boolean
        return resultado != -1L
    }

    // Metodo para Consultar a un Usuario
    fun iniciarSesion(correo: String, password: String) : Boolean{

        val db = readableDatabase

        val query_sql = """
            SELECT * FROM ${com.example.ep2_.DatabaseHelper.Companion.TABLE_USUARIOS} 
            WHERE ${com.example.ep2_.DatabaseHelper.Companion.COL_CORREO} = ? AND ${com.example.ep2_.DatabaseHelper.Companion.COL_PASSWORD} = ?
        """.trimIndent()

        // Crea cursor para recorrer la informacion
        val cursor = db.rawQuery(
            query_sql, arrayOf(correo, password)
        )

        // Crear variable de tipo Bool que permita saber si el cursor encontro un registro
        val usuarioEncontrardo = cursor.count > 0

        cursor.close()

        db.close()

        return usuarioEncontrardo

    }
}