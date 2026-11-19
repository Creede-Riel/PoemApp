package com.example.poemapp.Data


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.poemapp.model.Poem
import org.json.JSONArray
import kotlin.String

class DBHelper (context: Context):
    SQLiteOpenHelper(context,"poem.db",null,1) {
    override fun onCreate(db: SQLiteDatabase) {
        //创建储存用户账号密码的数据库
        db.execSQL(
            """
        create table users(id integer primary key autoincrement,
            username text,
            password text)
        """.trimIndent()
        )
        //创建用来装诗词资源的数据库
        db.execSQL("""
    create table poem (
        id text primary key,
        title text,
        author text,
        dynasty text,
        genre text,
        format text,
        collection text,
        theme text,
        content text,
        translate text,
        notes text,
        appreciation text,
        famous_line text,
        author_bio text
    )
""".trimIndent())


    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    //注册把账号密码存入数据库
    fun register(username: String, password: String) {
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
        }
        //这个是打包
        writableDatabase.insert("users", null, values)
    }
    //这个是插入数据
    fun checkLogin(username: String, password: String): Boolean {
        val cursor = writableDatabase.query(
            "users", null, "username = ?", arrayOf(username), null, null, null
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }
        //查找账号是否存在

        val dbPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))
        cursor.close()
        return dbPassword == password
    //账号存在就去读取密码，检查登录时输入的密码是否注册时输入的密码相同
    }

    // 检查诗有多少条（验证用）
    fun poemCount(): Int {
        val cursor = readableDatabase.rawQuery("select count(*) from poem", null)//数出诗词总数
        cursor.moveToFirst()//因为cursor只返回一行一列，即一个值，所以把cursor移到第一行
        val count = cursor.getInt(0)//获取第一行第0列的数据，即刚刚数出来的诗词总数
        cursor.close()
        return count//返回数据
    }
//导入诗词数据的函数
    fun importPoems(context: Context) {
        // 诗词数量大于零说明已经导入过数据，导过了就不重复导
        if (poemCount() > 0) return
        //下面是在把assets里面的文件读成字符串
        val json = context.assets.open("poems_app.json")//打开assets文件
            .bufferedReader()//这是那个储存字节并拼成字符串的缓冲带
            .use { it.readText() }//一口气读完，转换为字符串

        val array = JSONArray(json)//把字符串变为数组
        val db = writableDatabase

        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            //打包
            val values = ContentValues().apply {
                put("id", o.getString("id"))
                put("title", o.getString("title"))
                put("author", o.getString("author"))
                put("dynasty", o.getString("dynasty"))
                put("genre", o.getString("genre"))
                put("format", o.getString("format"))
                put("collection", o.getString("collection"))
                put("theme", o.getString("theme"))
                put("content", o.getString("content"))
                put("translate", o.getString("translate"))
                put("notes", o.getString("notes"))
                put("appreciation", o.getString("appreciation"))
                put("famous_line", o.getString("famousLine"))
                put("author_bio", o.getString("authorBio"))
            }
            //插入
            db.insert("poem", null, values)
        }
    }
//将所有的数据库内的诗词查出来交给一个list
    fun queryAllPoems(): List<Poem> {
        val list = mutableListOf<Poem>()//准备一个可变的集合

        val cursor = readableDatabase.query(
            "poem",
            arrayOf("id", "title", "author", "dynasty", "content", "famous_line"),
            null, null, null, null, null
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Poem(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        author = c.getString(c.getColumnIndexOrThrow("author")),
                        dynasty=c.getString(c.getColumnIndexOrThrow("dynasty")),
                        content=c.getString(c.getColumnIndexOrThrow("content")),
                        famousLine=c.getString(c.getColumnIndexOrThrow("famous_line")),
                    )
                )
            }
        }
        return list
    }

}

