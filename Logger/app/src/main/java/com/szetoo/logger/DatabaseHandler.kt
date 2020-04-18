package com.szetoo.logger

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DatabaseHandler(context: Context, tableName: String) : SQLiteOpenHelper(context, DatabaseHandler.DB_NAME, null, DatabaseHandler.DB_VERSION) {

    val name : String = tableName
    val TABLE_NAME = "\"" + tableName + "\""

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                ID + " INTEGER PRIMARY KEY," +
                DATE + " TEXT," + VALUE + " REAL);"
        db.execSQL(CREATE_TABLE)
    }

    override fun onOpen(db: SQLiteDatabase) {
        val CREATE_NEW_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                ID + " INTEGER PRIMARY KEY," +
                DATE + " TEXT," + VALUE + " REAL);"
        db.execSQL(CREATE_NEW_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME
        db.execSQL(DROP_TABLE)
        onCreate(db)
    }

    fun addValue(loggingData: LoggingData): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(DATE, loggingData.date)
        values.put(VALUE, loggingData.value)
        val _success = db.insert(TABLE_NAME, null, values)
        db.close()
        return (Integer.parseInt("$_success") != -1)
    }

    fun getValue(_id: Int): LoggingData {
        val loggingData = LoggingData()
        val db = writableDatabase
        val selectQuery = "SELECT  * FROM $TABLE_NAME WHERE $ID = ${_id.toString()}"
        //val selectQuery = "SELECT  * FROM $TABLE_NAME"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor != null) {
            cursor.moveToFirst()
            loggingData.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)))
            loggingData.value= parseDouble(cursor.getString(cursor.getColumnIndex(VALUE)))
            loggingData.date = cursor.getString(cursor.getColumnIndex(DATE))
        }
        cursor.close()
        return loggingData
    }

    val allDataValues: ArrayList<LoggingData>
        get() {
            val valuesList = ArrayList<LoggingData>()
            val db = writableDatabase
            val selectQuery = "SELECT  * FROM $TABLE_NAME"
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor != null) {
                cursor.moveToFirst()
                for (i in 1..cursor.count){
                    val loggingData = LoggingData()
                    loggingData.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ID)))
                    loggingData.value = parseDouble(cursor.getString(cursor.getColumnIndex(VALUE)))
                    loggingData.date = cursor.getString(cursor.getColumnIndex(DATE))
                    valuesList.add(loggingData)
                    cursor.moveToNext()
                }
            }
            cursor.close()
            return valuesList
        }

    fun updateEntry(loggingData: LoggingData): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(VALUE, loggingData.value)
        values.put(DATE, loggingData.date)
        val _success = db.update(TABLE_NAME, values, ID + "=?", arrayOf(loggingData.id.toString())).toLong()
        db.close()
        return Integer.parseInt("$_success") != -1
    }

    fun deleteEntry(_id: Int): Boolean {
        val db = this.writableDatabase
        val _success = db.delete(TABLE_NAME, ID + "=?", arrayOf(_id.toString())).toLong()
        db.close()
        return Integer.parseInt("$_success") != -1
    }


    companion object {
        private val DB_VERSION = 1
        val DB_NAME = "MyTrackingData"
        //private val TABLE_NAME = "Weights"
        private val ID = "Id"
        private val VALUE = "Value"
        private val DATE = "Date"
    }
}

fun date2str(date: Date): String {
    val strDate = SimpleDateFormat("yyyyMMdd_HH:mm:ss").format(date)
    return strDate
}

