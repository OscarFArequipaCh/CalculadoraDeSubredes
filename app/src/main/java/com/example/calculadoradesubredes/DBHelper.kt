package com.example.calculadoradesubredes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "solicitudes.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "subredes"
        const val COLUMN_ID = "id"
        const val COLUMN_DIRECCIONRED = "direccionRed"
        const val COLUMN_RANGO = "rango"
        const val COLUMN_DIRECIONBROADCAST = "direccionBroadcast"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_DIRECCIONRED TEXT, $COLUMN_RANGO TEXT, $COLUMN_DIRECIONBROADCAST TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertarSubred(direccionRed: String, rango: String, direccionBroadcast: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DIRECCIONRED, direccionRed)
            put(COLUMN_RANGO, rango)
            put(COLUMN_DIRECIONBROADCAST, direccionBroadcast)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun obtenerSubredes(): List<Subred> {
        val listaSubredes = ArrayList<Subred>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val direccionRed = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECCIONRED))
                val rango = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RANGO))
                val direccionBroadcast = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECIONBROADCAST))
                listaSubredes.add(Subred(id, direccionRed, rango, direccionBroadcast))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return listaSubredes
    }

    fun actualizarSubred(subred: Subred): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DIRECCIONRED, subred.direccionRed)
            put(COLUMN_RANGO, subred.rango)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(subred.id.toString()))
    }

    fun borrarSubred(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun vaciarTablaSubredes(): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, null, null)
    }
}