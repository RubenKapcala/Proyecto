package com.juego.proyecto_1s2122.varios.BBDD

class Tablas {
    abstract class Jugadores{
        companion object{
            const val TABLE_NAME = "jugadores"
            const val COLUMN_id = "id"
            const val COLUMN_nombre = "nombre"
            const val COLUMN_alias = "alias"
        }
    }

    abstract class Juegos{
        companion object{
            const val TABLE_NAME = "juegos"
            const val COLUMN_id = "id"
            const val COLUMN_nombre = "nombre"
            const val COLUMN_descripcion = "descripcion"
        }
    }

    abstract class Partidas{
        companion object{
            const val TABLE_NAME = "partidas"
            const val COLUMN_id = "id"
            const val COLUMN_id_juego = "id_juego"
            const val COLUMN_fecha = "fecha"
        }
    }

    abstract class Partidas_jugadores{
        companion object{
            const val TABLE_NAME = "partidas_jugadores"
            const val COLUMN_id_partida = "id_partida"
            const val COLUMN_id_jugador = "id_jugador"
            const val COLUMN_puntos = "puntos"
        }
    }
}