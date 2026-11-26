package com.example.poemapp.Data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.R
import com.example.poemapp.Ui.DetailActivity
import com.example.poemapp.Ui.PoemAdapter
import com.example.poemapp.model.Poem
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



private lateinit var db: DBHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "poem.db", null, 4) {

    // ==================== 建表语句 ====================

    //创建储存用户账号密码的数据库
    private val createUsers = """
        create table users(
            id integer primary key autoincrement,
            username text,
            password text,
            nickname text      
        )
    """.trimIndent()

    //创建用来装诗词资源的数据库
    private val createPoem = """
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
    """.trimIndent()

    //创建用来装不同用户的收藏诗词的数据库
    private val createFavorite = """
        create table favorite (
            id integer primary key autoincrement,
            username text,
            poem_id text,
            created_at text
        )
    """.trimIndent()

    //创建用来装不同用户浏览历史的表
    private val createHistory = """
        create table history (
            id integer primary key autoincrement,
            username text,
            poem_id text,
            viewed_at text
        )
    """.trimIndent()

    // ==================== 建库 / 升级 ====================

    // 数据库文件不存在时执行一次：把四张表一次建好
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createUsers)
        db.execSQL(createPoem)
        db.execSQL(createFavorite)
        db.execSQL(createHistory)
    }

    // 数据库已存在、版本号变了时执行：老库补新表
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 版本 1 -> 2：补上 favorite 表（users / poem 的数据都保留）
        if (oldVersion < 2) {
            db.execSQL(createFavorite)
        }
        // 版本 2 -> 3：补上 history 表（同上，老数据都保留）
        if (oldVersion < 3) {
            db.execSQL(createHistory)
        }
        //版本3 -> 4，给用户表加上昵称列
        if (oldVersion < 4) {
            db.execSQL("alter table users add column nickname text")
        }
    }

    // ==================== 用户 ====================

    //注册把账号密码存入数据库
    fun register(username: String, password: String) {
        //打包
        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
        }
        //插入数据
        writableDatabase.insert("users", null, values)
    }
//检查登录时是否已经存在该账号
    fun checkLogin(username: String, password: String): Boolean {
        val cursor = writableDatabase.query(
            "users", null, "username = ?", arrayOf(username), null, null, null
        )

        //查找账号是否存在
        if (!cursor.moveToFirst()) {
            cursor.close()
            return false
        }

        //账号存在就去读取密码，检查登录时输入的密码是否和注册时输入的密码相同
        val dbPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"))
        cursor.close()
        return dbPassword == password
    }

    //判断用户名是否已被注册（用户账号唯一性）
    fun isUserExists(username: String): Boolean {
        val cursor = readableDatabase.query(
            "users", arrayOf("username"), "username = ?", arrayOf(username), null, null, null
        )
        cursor.use { c ->
            return c.moveToFirst()      // 有一行 = 已存在
        }
    }

    //修改密码
    fun updatePassword(username: String, newPassword: String) {
        val values = ContentValues().apply {
            put("password", newPassword)
        }
        writableDatabase.update(
            "users",
            values,
            "username = ?",
            arrayOf(username)
        )
    }

    //查昵称（没有就返回空字符串）
    fun getNickname(username: String): String {
        val cursor = readableDatabase.query(
            "users", arrayOf("nickname"), "username = ?", arrayOf(username), null, null, null
        )
        cursor.use { c ->
            if (!c.moveToFirst()) return ""
            return c.getString(c.getColumnIndexOrThrow("nickname")) ?: ""
        }
    }

    //改昵称
    fun updateNickname(username: String, nickname: String) {
        val values = ContentValues().apply {
            put("nickname", nickname)
        }
        writableDatabase.update("users", values, "username = ?", arrayOf(username))
    }

    // ==================== 诗词：导入 / 查询 ====================

    // 检查诗有多少条（验证用）
    fun poemCount(): Int {
        //数出诗词总数
        val cursor = readableDatabase.rawQuery("select count(*) from poem", null)
        //因为cursor只返回一行一列，即一个值，所以把cursor移到第一行
        cursor.moveToFirst()
        //获取第一行第0列的数据，即刚刚数出来的诗词总数
        val count = cursor.getInt(0)
        cursor.close()
        //返回数据
        return count
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
                        dynasty = c.getString(c.getColumnIndexOrThrow("dynasty")),
                        content = c.getString(c.getColumnIndexOrThrow("content")),
                        famousLine = c.getString(c.getColumnIndexOrThrow("famous_line"))
                    )
                )
            }
        }
        return list
    }

    //一个专门用于获取详情页诗词所有信息的函数
    fun queryPoemById(id: String): Poem? {
        val cursor = readableDatabase.query(
            "poem", null, "id = ?", arrayOf(id), null, null, null
        )
        //按照传进去的id查询对应的诗词的全部数据
        cursor.use { c ->
            if (!c.moveToFirst()) return null
            return Poem(
                id = c.getString(c.getColumnIndexOrThrow("id")),
                title = c.getString(c.getColumnIndexOrThrow("title")),
                author = c.getString(c.getColumnIndexOrThrow("author")),
                dynasty = c.getString(c.getColumnIndexOrThrow("dynasty")),
                content = c.getString(c.getColumnIndexOrThrow("content")),
                famousLine = c.getString(c.getColumnIndexOrThrow("famous_line")),
                genre = c.getString(c.getColumnIndexOrThrow("genre")),
                format = c.getString(c.getColumnIndexOrThrow("format")),
                collection = c.getString(c.getColumnIndexOrThrow("collection")),
                theme = c.getString(c.getColumnIndexOrThrow("theme")),
                translate = c.getString(c.getColumnIndexOrThrow("translate")),
                notes = c.getString(c.getColumnIndexOrThrow("notes")),
                appreciation = c.getString(c.getColumnIndexOrThrow("appreciation")),
                authorBio = c.getString(c.getColumnIndexOrThrow("author_bio"))
            )
        }
    }

    // ==================== 收藏 ====================

    //用来添加收藏诗词
    fun addFavorite(username: String, poemId: String) {
        //已经收藏过就不再插入，防止出现两条一样的
        if (isFavorite(username, poemId)) return

        //打包
        val values = ContentValues().apply {
            put("username", username)
            put("poem_id", poemId)
            put(
                "created_at",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
        }
        //插入
        writableDatabase.insert("favorite", null, values)
    }

    //取消收藏
    fun removeFavorite(username: String, poemId: String) {
        writableDatabase.delete(
            "favorite",
            "username = ? and poem_id = ?",
            arrayOf(username, poemId)
        )
    }

    //判断这一首诗有没有被这个用户收藏
    fun isFavorite(username: String, poemId: String): Boolean {
        val cursor = readableDatabase.query(
            "favorite", null,
            "username = ? and poem_id = ?",
            arrayOf(username, poemId),
            null, null, null
        )
        cursor.use { c ->
            //有这条就是 true，没有就是 false
            return c.moveToFirst()
        }
    }

    //查出这个用户收藏的所有诗（两张表用 id 配对）
    fun queryFavorites(username: String): List<Poem> {
        val cursor = readableDatabase.rawQuery(
            "select p.* from poem p inner join favorite f on p.id = f.poem_id where f.username = ?",
            arrayOf(username)
        )
        val list = mutableListOf<Poem>()//准备一个可变的集合
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Poem(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        author = c.getString(c.getColumnIndexOrThrow("author")),
                        dynasty = c.getString(c.getColumnIndexOrThrow("dynasty")),
                        content = c.getString(c.getColumnIndexOrThrow("content")),
                        famousLine = c.getString(c.getColumnIndexOrThrow("famous_line"))
                    )
                )
            }
        }
        return list
    }

    // ==================== 浏览历史 ====================

    //增添浏览记录的函数，包括去旧保新
    fun addHistory(username: String, poemId: String) {
        // 先删掉这首诗的旧记录（去重：同一首只留一条）
        writableDatabase.delete(
            "history",
            "username = ? and poem_id = ?",
            arrayOf(username, poemId)
        )
        // 再插入新的一条（时间是最新的），精确到秒
        val values = ContentValues().apply {
            put("username", username)
            put("poem_id", poemId)
            put(
                "viewed_at",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
        }
        writableDatabase.insert("history", null, values)
    }

    //查询浏览记录里面的诗词（最近看的在最上面）
    fun queryHistory(username: String): List<Poem> {
        val cursor = readableDatabase.rawQuery(
            "select p.* from poem p inner join history h on p.id = h.poem_id " +
                    "where h.username = ? order by h.viewed_at desc, h.id desc",
            arrayOf(username)
        )
        val list = mutableListOf<Poem>()
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Poem(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        author = c.getString(c.getColumnIndexOrThrow("author")),
                        dynasty = c.getString(c.getColumnIndexOrThrow("dynasty")),
                        content = c.getString(c.getColumnIndexOrThrow("content")),
                        famousLine = c.getString(c.getColumnIndexOrThrow("famous_line"))
                    )
                )
            }
        }
        return list
    }

    //清空这个用户的浏览历史
    fun clearHistory(username: String) {
        writableDatabase.delete(
            "history",
            "username = ?",
            arrayOf(username)
        )
    }

// ==================== 筛选功能 ====================

    //查出数据库里一共有哪些朝代（去重）
    fun queryAllDynasties(): List<String> {
        val list = mutableListOf<String>()
        val cursor = readableDatabase.rawQuery(
            "select distinct dynasty from poem",
            null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(c.getString(c.getColumnIndexOrThrow("dynasty")))
            }
        }
        return list
    }


    //查诗词对应的朝代
    fun queryPoemsByDynasty(dynasty: String): List<Poem> {
        val list = mutableListOf<Poem>()
        val cursor = readableDatabase.query(
            "poem",
            arrayOf("id", "title", "author", "dynasty", "content", "famous_line"),
            "dynasty = ?",
            arrayOf(dynasty),
            null, null, null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Poem(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        author = c.getString(c.getColumnIndexOrThrow("author")),
                        dynasty = c.getString(c.getColumnIndexOrThrow("dynasty")),
                        content = c.getString(c.getColumnIndexOrThrow("content")),
                        famousLine = c.getString(c.getColumnIndexOrThrow("famous_line"))
                    )
                )
            }
        }
        return list
    }

    // ==================== 搜索功能 ====================
    //按关键词搜索（标题 / 作者 / 正文）
    fun queryPoemsByKeyword(keyword: String): List<Poem> {
        val list = mutableListOf<Poem>()
        val pattern = "%$keyword%"
        val cursor = readableDatabase.query(
            "poem",
            arrayOf("id", "title", "author", "dynasty", "content", "famous_line"),
            "title like? or author like? or content like?",
            arrayOf(pattern, pattern, pattern),
            null, null, null
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Poem(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        author = c.getString(c.getColumnIndexOrThrow("author")),
                        dynasty = c.getString(c.getColumnIndexOrThrow("dynasty")),
                        content = c.getString(c.getColumnIndexOrThrow("content")),
                        famousLine = c.getString(c.getColumnIndexOrThrow("famous_line"))
                    )
                )
            }
        }
        return list
    }
}
