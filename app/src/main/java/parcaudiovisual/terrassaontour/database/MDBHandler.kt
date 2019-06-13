package parcaudiovisual.terrassaontour.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MDBHandler(context: Context, nombreDB: String?,factory: SQLiteDatabase.CursorFactory?,version: Int): SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION ) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "terrassaOnTourDB.db"

        val TABLE_PUNTOS = "puntos"
        val PUNTOS_COLUMN_ID = "id"
        val PUNTOS_COLUMN_TITLE = "title"
        val PUNTOS_COLUMN_LAT = "lat"
        val PUNTOS_COLUMN_LON = "lon"
        val PUNTOS_COLUMN_IMG_URL = "img_url"
        val PUNTOS_COLUMN_IMG_URL_BIG = "img_url_big"
        val PUNTOS_COLUMN_IMG_URL_BIG_SECUNDARY = "img_url_big_secundary"
        val PUNTOS_COLUMN_EXTERIOR = "exterior"
        val PUNTOS_COLUMN_DIA = "dia"



    }

    override fun onCreate(db: SQLiteDatabase?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}