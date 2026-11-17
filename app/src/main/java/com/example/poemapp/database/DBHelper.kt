package com.example.poemapp.database


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
//创建数据库
class DBHelper (context: Context):
    SQLiteOpenHelper(context,"poem.db",null,1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
        create table users(id integer primary key autoincrement,
            username text,
            password text)
        """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun register(username: String, password: String) {//注册把账号密码存入数据库
        val values = ContentValues().apply {//这个是打包
            put("username", username)
            put("password", password)
        }
        writableDatabase.insert("users", null, values)//这个是插入数据
    }

    fun checkLogin(username: String, password: String): Boolean {
        val cursor = writableDatabase.query(
            "users", null, "username = ?", arrayOf(username), null, null, null
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }//查找账号是否存在

        val dbPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))
        cursor.close()
        return dbPassword == password//账号存在就去读取密码，检查登录时输入的密码是否注册时输入的密码相同
    }

}

