package com.juego.proyecto_1s2122.modelo.BBDD

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.juego.proyecto_1s2122.R
import com.juego.proyecto_1s2122.modelo.Juego
import com.juego.proyecto_1s2122.modelo.Jugador
import com.juego.proyecto_1s2122.modelo.Partida

class DbHelper(private var context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val db: SQLiteDatabase = this.writableDatabase//Representa la BBDD

    //Declaración de constantes
    companion object{
        private const val DATABASE_NAME = "miBBDD" //Nombre de la BBDD
        private const val DATABASE_VERSION = 1 //Versión de la BBDD
    }

    //Introduce los datos del jugador en la BBDD
    fun guardarUsuario(nombre: String, alias: String){
        val values = ContentValues() //Agrupa los valores a insertar
        values.put(Tablas.Jugadores.COLUMN_nombre, nombre) //Introduce el nombre
        values.put(Tablas.Jugadores.COLUMN_alias, alias) //Introduce el alias
        db.insert(Tablas.Jugadores.TABLE_NAME, null, values) //Realiza el insert
    }

    //Recupera los datos del jugador
    fun obtenerUsuario(): Jugador?{
        //Inicializa las variables
        val nombre: String?
        val alias: String?
        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.query(Tablas.Jugadores.TABLE_NAME,
                arrayOf(Tablas.Jugadores.COLUMN_nombre, Tablas.Jugadores.COLUMN_alias),
                "id = 1", null, null, null, null)
        //Saca del cursor la información y con los datos crea un objeto Jugador
        if (cursor.moveToFirst()){
            nombre = cursor.getString(0) //Obtiene el nombre
            alias = cursor.getString(1) //Obtiene el alias
            cursor.close() //Cierra el cursor
            return Jugador(nombre, alias, 0) //Devuelve el jugador
        }
        cursor.close()//Cierra el cursor
        return null //En caso de que no se pueda realizar la query devuelve null
    }

    //Devuelve una lista con todos los juegos
    @SuppressLint("Range") //El valor siempre será positivo
    fun obtenerJuegos(): List<Juego>{
        //Inicializa las variables
        val lista = mutableListOf<Juego>()
        var id: Int?
        var nombre: String?
        var descripcion: String?

        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.rawQuery("select * from " + Tablas.Juegos.TABLE_NAME, null)

        //Recorre el cursor y guarda la información en un objeto Juego para añadirlo a la lista
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(Tablas.Juegos.COLUMN_id))
            nombre = cursor.getString(cursor.getColumnIndex(Tablas.Juegos.COLUMN_nombre))
            descripcion = cursor.getString(cursor.getColumnIndex(Tablas.Juegos.COLUMN_descripcion))
            val juego = Juego(id, nombre, descripcion)
            lista.add(juego) //Añade el juego a la lista
        }
        cursor.close() //Cierra el cursor
        return lista //Devuelve la lista
    }

    //Devuelve una lista con todos los jugadores
    @SuppressLint("Range") //El valor siempre será positivo
    fun obtenerJugadores(): List<Jugador>{
        //Inicializa las variables
        val lista = mutableListOf<Jugador>()
        var id : Int
        var nombre: String
        var alias: String

        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.rawQuery("SELECT * FROM " + Tablas.Jugadores.TABLE_NAME, null)

        //Recorre el cursor y guarda la información en un objeto Jugador para añadirlo a la lista
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_id))
            nombre = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_nombre))
            alias = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_alias))
            val jugador = Jugador(nombre, alias, 0, id)
            lista.add(jugador)//Añade el Jugador a la lista
        }
        cursor.close() //Cierra el cursor
        return lista //Devuelve la lista
    }

    //Devuelve una lista con todas las partidas
    @SuppressLint("Range") //El valor siempre será positivo
    fun obtenerPartidas(): List<Partida>{
        //Inicializa la lista
        val lista = mutableListOf<Partida>()

        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.rawQuery("SELECT * FROM " + Tablas.Partidas.TABLE_NAME,
                null)

        //Recorre el cursor y guarda la información en un objeto Partida para añadirlo a la lista
        while (cursor.moveToNext()) {
            val fecha = cursor.getString(cursor.getColumnIndex(Tablas.Partidas.COLUMN_fecha))
            val idpartida = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas.COLUMN_id))
            val idJuego = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas.COLUMN_id_juego))

            val jugadores = obtenerJugadoresPartida(idpartida)
            val juego = obtenerJuego(idJuego)

            val partida = Partida(jugadores, juego, jugadores.size, fecha)
            lista.add(partida) //Añade la partida
        }
        cursor.close() //Cierra el cursor
        return lista //Devuelve la lista
    }

    //Devuelve una lista con todas las partidas jugadas por un jugador
    @SuppressLint("Range") //El valor siempre será positivo
    fun obtenerPartidasDeJugador(idJugador: Int): List<Partida>{
        //Inicializa la lista
        val lista = mutableListOf<Partida>()

        //Realiza la query filtrando por el Id del jugador y guarda el resultado en un cursor
        val cursor = db.rawQuery(
                "SELECT p.* FROM " + Tablas.Partidas.TABLE_NAME + " AS p" +
                        " JOIN " + Tablas.Partidas_jugadores.TABLE_NAME + " AS j" +
                        " ON p." + Tablas.Partidas.COLUMN_id + " = j." + Tablas.Partidas_jugadores.COLUMN_id_partida +
                        " WHERE " + Tablas.Partidas_jugadores.COLUMN_id_jugador + " = " + idJugador.toString()
                , null)

        //Recorre el cursor y guarda la información en un objeto Partida para añadirlo a la lista
        while (cursor.moveToNext()) {
            val fecha = cursor.getString(cursor.getColumnIndex(Tablas.Partidas.COLUMN_fecha))
            val idpartida = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas.COLUMN_id))
            val idJuego = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas.COLUMN_id_juego))

            val jugadores = obtenerJugadoresPartida(idpartida)
            val juego = obtenerJuego(idJuego)

            val partida = Partida(jugadores, juego, jugadores.size, fecha)
            lista.add(partida) //Añade la partida
        }
        cursor.close() //Cierra el cursor
        return lista //Devuelve la lista
    }

    //Devuelve una lista con todos los jugadores que jugaron una partida
    @SuppressLint("Range") //El valor siempre será positivo
    private fun obtenerJugadoresPartida(idPartida: Int): MutableList<Jugador>{
        //Inicializa las variables
        val lista = mutableListOf<Jugador>()
        var id : Int
        var nombre: String
        var alias: String
        var puntos : Int

        //Realiza la query filtrando por el Id de la partida y guarda el resultado en un cursor
        val cursor = db.rawQuery(
                    "SELECT j.*, p." + Tablas.Partidas_jugadores.COLUMN_puntos +
                        " FROM " + Tablas.Jugadores.TABLE_NAME + " AS j" +
                        " JOIN " + Tablas.Partidas_jugadores.TABLE_NAME + " AS p" +
                        " ON j." + Tablas.Jugadores.COLUMN_id + " = p." + Tablas.Partidas_jugadores.COLUMN_id_jugador +
                        " WHERE " + Tablas.Partidas_jugadores.COLUMN_id_partida + " = " + idPartida.toString()
                , null)

        //Recorre el cursor y guarda la información en un objeto Jugador para añadirlo a la lista
        while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_id))
            nombre = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_nombre))
            alias = cursor.getString(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_alias))
            puntos = cursor.getInt(cursor.getColumnIndex(Tablas.Partidas_jugadores.COLUMN_puntos))
            val jugador = Jugador(nombre, alias, puntos, id)
            lista.add(jugador) //Añade el jugador
        }
        cursor.close() //Cierra el cursor
        return lista //Devuelve la lista
    }

    //Devuelve un juego a partir de su Id
    @SuppressLint("Range") //El valor siempre será positivo
    private fun obtenerJuego(idJuego: Int): Juego{
        //Inicializa las variables
        var id = 0
        var nombre = ""
        var descripcion = ""

        //Realiza la query y guarda el resultado en un cursor
        val cursor = db.rawQuery("select * from " + Tablas.Juegos.TABLE_NAME+
                " WHERE " + Tablas.Juegos.COLUMN_id + " = " + idJuego.toString()
                , null)

        //Saca del cursor la información
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(Tablas.Juegos.COLUMN_id))
            nombre = cursor.getString(cursor.getColumnIndex(Tablas.Juegos.COLUMN_nombre))
            descripcion = cursor.getString(cursor.getColumnIndex(Tablas.Juegos.COLUMN_descripcion))
        }

        cursor.close() //Cierra el cursor
        return Juego(id, nombre, descripcion) //Crea un objeto Juego con la información y lo devuelve
    }

    //Guarda en la BBDD la partida que se pasa por parametro
    @SuppressLint("Range") //El valor siempre será positivo
    fun guardarPartida(partida: Partida) {

        //Adapta el Id a la bbdd propia
        for (jugador in partida.jugadores){
            //Realiza una búsqueda en la BBDD por el nombre y el alias de cada jugador
            val cursor = db.rawQuery("SELECT " + Tablas.Jugadores.COLUMN_id +
                    " FROM " + Tablas.Jugadores.TABLE_NAME +
                    " WHERE " + Tablas.Jugadores.COLUMN_nombre + " = '" + jugador.nombre +
                    "' AND " + Tablas.Jugadores.COLUMN_alias + " = '" + jugador.alias + "'",
                    null)

            //Si ese jugador aún no existe en la BBDD hace un insert con sus datos
            if (!cursor.moveToFirst()){
                val values = ContentValues() //Agrupa los valores a insertar
                values.put(Tablas.Jugadores.COLUMN_nombre, jugador.nombre) //Introduce el nombre
                values.put(Tablas.Jugadores.COLUMN_alias, jugador.alias) //Introduce el alias
                jugador.id = db.insert(Tablas.Jugadores.TABLE_NAME, null, values).toInt()
            }else{ //Si Existe en la BBDD da la jugador el id que tiene en la BBDD
                jugador.id = cursor.getInt(cursor.getColumnIndex(Tablas.Jugadores.COLUMN_id))
            }
            cursor.close() //Cierra el cursor
        }

        //Guarda la partida
        val valores = ContentValues() //Agrupa los valores a insertar
        valores.put(Tablas.Partidas.COLUMN_fecha, partida.fecha) //Introduce la fecha
        valores.put(Tablas.Partidas.COLUMN_id_juego, partida.juego.id) //Introduce el Id del juego
        val partidaId = db.insert(Tablas.Partidas.TABLE_NAME, null, valores)

        //Guarda las puntuaciones
        for (jugador in partida.jugadores){
            val values = ContentValues() //Agrupa los valores a insertar
            values.put(Tablas.Partidas_jugadores.COLUMN_id_partida, partidaId) //Introduce el Id de la partida
            values.put(Tablas.Partidas_jugadores.COLUMN_id_jugador, jugador.id) //Introduce el Id del jugador
            values.put(Tablas.Partidas_jugadores.COLUMN_puntos, jugador.puntos) //Introduce los puntos del jugador
            db.insert(Tablas.Partidas_jugadores.TABLE_NAME, null, values)
        }
    }

    //Si la BBDD no existe llama a esta función para crearla
    override fun onCreate(db: SQLiteDatabase?) {
        //Crea la tabla jugadores
        db!!.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Jugadores.TABLE_NAME} (" +
                "${Tablas.Jugadores.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Jugadores.COLUMN_nombre} TEXT NOT NULL, " +
                "${Tablas.Jugadores.COLUMN_alias} TEXT NOT NULL)")

        //Crea la tabla juegos
        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Juegos.TABLE_NAME} (" +
                "${Tablas.Juegos.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Juegos.COLUMN_nombre} TEXT NOT NULL, " +
                "${Tablas.Juegos.COLUMN_descripcion} TEXT NOT NULL)")


        //Crea la tabla partidas
        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Partidas.TABLE_NAME} (" +
                "${Tablas.Partidas.COLUMN_id} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${Tablas.Partidas.COLUMN_id_juego} INTEGER NOT NULL, " +
                "${Tablas.Partidas.COLUMN_fecha} TEXT NOT NULL, " +
                "FOREIGN KEY(${Tablas.Partidas.COLUMN_id_juego}) REFERENCES ${Tablas.Juegos.TABLE_NAME}(${Tablas.Juegos.COLUMN_id}))")

        //Crea la tabla partidas_jugadores
        db.execSQL("CREATE TABLE IF NOT EXISTS ${Tablas.Partidas_jugadores.TABLE_NAME} (" +
                "${Tablas.Partidas_jugadores.COLUMN_id_partida} INTEGER NOT NULL, " +
                "${Tablas.Partidas_jugadores.COLUMN_id_jugador} INTEGER NOT NULL, " +
                "${Tablas.Partidas_jugadores.COLUMN_puntos} INTEGER NOT NULL, " +
                "FOREIGN KEY(${Tablas.Partidas_jugadores.COLUMN_id_partida}) REFERENCES ${Tablas.Partidas.TABLE_NAME}(${Tablas.Partidas.COLUMN_id}), " +
                "FOREIGN KEY(${Tablas.Partidas_jugadores.COLUMN_id_jugador}) REFERENCES ${Tablas.Jugadores.TABLE_NAME}(${Tablas.Jugadores.COLUMN_id}))")

        //Introduce el primer juego
        var values = ContentValues() //Agrupa los valores a insertar
        values.put(Tablas.Juegos.COLUMN_nombre, context.getString(R.string.pulsar)) //Inserta el nombre del juego
        values.put(Tablas.Juegos.COLUMN_descripcion, context.getString(R.string.descripcion_pulsar)) //Inserta la descripción del juego
        db.insert(Tablas.Juegos.TABLE_NAME, null, values) //Realiza el insert

        //Introduce el segundo juego
        values = ContentValues()
        values.put(Tablas.Juegos.COLUMN_nombre, context.getString(R.string.frotar)) //Inserta el nombre del juego
        values.put(Tablas.Juegos.COLUMN_descripcion, context.getString(R.string.descripcion_frotar)) //Inserta la descripción del jue
        db.insert(Tablas.Juegos.TABLE_NAME, null, values) //Realiza el insert

        //Introduce el tercer juego
        values = ContentValues()
        values.put(Tablas.Juegos.COLUMN_nombre, context.getString(R.string.globos)) //Inserta el nombre del juego
        values.put(Tablas.Juegos.COLUMN_descripcion, context.getString(R.string.descripcion_globos)) //Inserta la descripción del jue
        db.insert(Tablas.Juegos.TABLE_NAME, null, values) //Realiza el insert

    }

    //Se llama a esta función cuando hay un cambio en la versión de la BBDD
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

}