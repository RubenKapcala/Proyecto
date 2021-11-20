package com.juego.proyecto_1s2122.varios.BBDD

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Juego
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.modelo.Partida

class DbHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var context: Context = context
    private val db: SQLiteDatabase = this.writableDatabase

    companion object{
        private const val DATABASE_NAME = "miBBDD"
        private const val DATABASE_VERSION = 1
    }

    fun guardarUsuario(nombre: String, alias: String){
        val values = ContentValues()
        values.put(Tablas.Jugadores.COLUMN_nombre, nombre)
        values.put(Tablas.Jugadores.COLUMN_alias, alias)
        db.insert(Tablas.Jugadores.TABLE_NAME, null, values)
    }

    fun obtenerUsuario(): Jugador?{
        val nombre: String?
        val alias: String?
        val cursor = db.query(Tablas.Jugadores.TABLE_NAME,
                arrayOf(Tablas.Jugadores.COLUMN_nombre, Tablas.Jugadores.COLUMN_alias),
                "id = 1", null, null, null, null)
        if (cursor.moveToFirst()){
            nombre = cursor.getString(0)
            alias = cursor.getString(1)
            cursor.close()
            return Jugador(nombre, alias, 0)
        }
        cursor.close()
        return null
    }

    @SuppressLint("Range")
    fun obtenerJuegos(): List<Juego>{
        val lista = mutableListOf<Juego>()
        var id: Int?
        var nombre: String?
        var descripcion: String?

        val cursor = db.rawQuery("select * from " + Tablas.Juegos.TABLE_NAME, null)

        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(Tablas.Juegos.COLUMN_id))
            nombre = cursor.getString(cursor.getColumnIndex(Tablas.Juegos.COLUMN_nombre))
            descripcion = cursor.getString(cursor.getColumnIndex(Tablas.Juegos.COLUMN_descripcion))
            val juego = Juego(id, nombre, descripcion)
            lista.add(juego)
        }

        cursor.close()
        return lista
    }

    @SuppressLint("Range")
    fun obtenerJugadores(): List<Jugador>{
        val lista = mutableListOf<Jugador>()
        val cursor = db.rawQuery("SELECT * FROM " + Tablas.Jugadores.TABLE_NAME, null)

        var id : Int
        var nombre: String
        var alias: String
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_id))
            nombre = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_nombre))
            alias = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_alias))
            val jugador = Jugador(nombre, alias, 0, id)
            lista.add(jugador)
        }
        cursor.close()
        return lista
    }

    @SuppressLint("Range")
    fun obtenerPartidas(): List<Partida>{
        val lista = mutableListOf<Partida>()

        val cursor = db.rawQuery("SELECT * FROM " + Tablas.Partidas.TABLE_NAME,
                null)

        while (cursor.moveToNext()) {
            val fecha = cursor.getString(cursor.getColumnIndex(Tablas.Partidas.COLUMN_fecha))
            val idpartida = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas.COLUMN_id))
            val idJuego = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas.COLUMN_id_juego))

            val jugadores = obtenerJugadoresPartida(idpartida)
            val juego = obtenerJuego(idJuego)

            val partida = Partida(jugadores, juego, jugadores.size, fecha)
            lista.add(partida)
        }
        cursor.close()

        return lista
    }

    @SuppressLint("Range")
    fun obtenerPartidasDeJugador(idJugador: Int): List<Partida>{
        val lista = mutableListOf<Partida>()

        val cursor = db.rawQuery(
                "SELECT p.* FROM " + Tablas.Partidas.TABLE_NAME + " AS p" +
                        " JOIN " + Tablas.Partidas_jugadores.TABLE_NAME + " AS j" +
                        " ON p." + Tablas.Partidas.COLUMN_id + " = j." + Tablas.Partidas_jugadores.COLUMN_id_partida +
                        " WHERE " + Tablas.Partidas_jugadores.COLUMN_id_jugador + " = " + idJugador.toString()
                , null)

        while (cursor.moveToNext()) {
            val fecha = cursor.getString(cursor.getColumnIndex(Tablas.Partidas.COLUMN_fecha))
            val idpartida = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas.COLUMN_id))
            val idJuego = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas.COLUMN_id_juego))

            val jugadores = obtenerJugadoresPartida(idpartida)
            val juego = obtenerJuego(idJuego)

            val partida = Partida(jugadores, juego, jugadores.size, fecha)
            lista.add(partida)
        }
        cursor.close()

        return lista
    }

    @SuppressLint("Range")
    private fun obtenerJugadoresPartida(idPartida: Int): MutableList<Jugador>{
        val lista = mutableListOf<Jugador>()
        val cursor = db.rawQuery(
                    "SELECT j.*, p." + Tablas.Partidas_jugadores.COLUMN_puntos +
                        " FROM " + Tablas.Jugadores.TABLE_NAME + " AS j" +
                        " JOIN " + Tablas.Partidas_jugadores.TABLE_NAME + " AS p" +
                        " ON j." + Tablas.Jugadores.COLUMN_id + " = p." + Tablas.Partidas_jugadores.COLUMN_id_jugador +
                        " WHERE " + Tablas.Partidas_jugadores.COLUMN_id_partida + " = " + idPartida.toString()
                , null)

        var id : Int
        var nombre: String
        var alias: String
        var puntos : Int
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_id))
            nombre = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_nombre))
            alias = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_alias))
            puntos = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas_jugadores.COLUMN_puntos))
            val jugador = Jugador(nombre, alias, puntos, id)
            lista.add(jugador)
        }
        cursor.close()
        return lista
    }

    @SuppressLint("Range")
    private fun obtenerJuego(idJuego: Int): Juego{
        var id = 0
        var nombre = ""
        var descripcion = ""

        val cursor = db.rawQuery("select * from " + Tablas.Juegos.TABLE_NAME+
                " WHERE " + Tablas.Juegos.COLUMN_id + " = " + idJuego.toString()
                , null)

        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(Tablas.Juegos.COLUMN_id))
            nombre = cursor.getString(cursor.getColumnIndex(Tablas.Juegos.COLUMN_nombre))
            descripcion = cursor.getString(cursor.getColumnIndex(Tablas.Juegos.COLUMN_descripcion))
        }

        cursor.close()
        return Juego(id, nombre, descripcion)
    }

    @SuppressLint("Range")
    fun guardarPartida(partida: Partida) {

        //Adapta el Id a la bbdd propia
        for (jugador in partida.jugadores){
            val cursor = db.rawQuery("SELECT " + Tablas.Jugadores.COLUMN_id +
                    " FROM " + Tablas.Jugadores.TABLE_NAME +
                    " WHERE " + Tablas.Jugadores.COLUMN_nombre + " = '" + jugador.nombre +
                    "' AND " + Tablas.Jugadores.COLUMN_alias + " = '" + jugador.alias + "'",
                    null)
            if (!cursor.moveToFirst()){
                val values = ContentValues()
                values.put(Tablas.Jugadores.COLUMN_nombre, jugador.nombre)
                values.put(Tablas.Jugadores.COLUMN_alias, jugador.alias)
                jugador.id = db.insert(Tablas.Jugadores.TABLE_NAME, null, values).toInt()
            }else{
                jugador.id = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_id))
            }
            cursor.close()
        }

        //Guarda la partida
        val valores = ContentValues()
        valores.put(Tablas.Partidas.COLUMN_fecha, partida.fecha)
        valores.put(Tablas.Partidas.COLUMN_id_juego, partida.juego.id)
        val partidaId = db.insert(Tablas.Partidas.TABLE_NAME, null, valores)

        //Guarda las puntuaciones
        for (jugador in partida.jugadores){
            val values = ContentValues()
            values.put(Tablas.Partidas_jugadores.COLUMN_id_partida, partidaId)
            values.put(Tablas.Partidas_jugadores.COLUMN_id_jugador, jugador.id)
            values.put(Tablas.Partidas_jugadores.COLUMN_puntos, jugador.puntos)
            db.insert(Tablas.Partidas_jugadores.TABLE_NAME, null, values)
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Jugadores.TABLE_NAME} (" +
                "${Tablas.Jugadores.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Jugadores.COLUMN_nombre} TEXT NOT NULL, " +
                "${Tablas.Jugadores.COLUMN_alias} TEXT NOT NULL)")


        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Juegos.TABLE_NAME} (" +
                "${Tablas.Juegos.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Juegos.COLUMN_nombre} TEXT NOT NULL, " +
                "${Tablas.Juegos.COLUMN_descripcion} TEXT NOT NULL)")



        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Partidas.TABLE_NAME} (" +
                "${Tablas.Partidas.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Partidas.COLUMN_id_juego} INTEGER NOT NULL, " +
                "${Tablas.Partidas.COLUMN_fecha} TEXT NOT NULL, " +
                "FOREIGN KEY(${Tablas.Partidas.COLUMN_id_juego}) REFERENCES ${Tablas.Juegos.TABLE_NAME}(${Tablas.Juegos.COLUMN_id}))")

        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Partidas_jugadores.TABLE_NAME} (" +
                "${Tablas.Partidas_jugadores.COLUMN_id_partida} INTEGER NOT NULL, " +
                "${Tablas.Partidas_jugadores.COLUMN_id_jugador} INTEGER NOT NULL, " +
                "${Tablas.Partidas_jugadores.COLUMN_puntos} INTEGER NOT NULL, " +
                "FOREIGN KEY(${Tablas.Partidas_jugadores.COLUMN_id_partida}) REFERENCES ${Tablas.Partidas.TABLE_NAME}(${Tablas.Partidas.COLUMN_id}), " +
                "FOREIGN KEY(${Tablas.Partidas_jugadores.COLUMN_id_jugador}) REFERENCES ${Tablas.Jugadores.TABLE_NAME}(${Tablas.Jugadores.COLUMN_id}))")

        var values = ContentValues()
        values.put(Tablas.Juegos.COLUMN_nombre, context.getString(R.string.pulsar))
        values.put(Tablas.Juegos.COLUMN_descripcion, context.getString(R.string.descripcion_pulsar))
        db.insert(Tablas.Juegos.TABLE_NAME, null, values)

        values = ContentValues()
        values.put(Tablas.Juegos.COLUMN_nombre, context.getString(R.string.frotar))
        values.put(Tablas.Juegos.COLUMN_descripcion, context.getString(R.string.descripcion_frotar))
        db.insert(Tablas.Juegos.TABLE_NAME, null, values)

        values = ContentValues()
        values.put(Tablas.Juegos.COLUMN_nombre, context.getString(R.string.globos))
        values.put(Tablas.Juegos.COLUMN_descripcion, context.getString(R.string.descripcion_globos))
        db.insert(Tablas.Juegos.TABLE_NAME, null, values)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

}