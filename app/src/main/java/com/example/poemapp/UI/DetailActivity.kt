package com.example.poemapp.UI

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details)

        // ① 拿到列表页传过来的 id
        val id = intent.getStringExtra("poemId")
        if (id == null) {
            finish()          // 没有 id，直接关掉
            return
        }

        // ② 按 id 查完整数据
        val poem = DBHelper(this).queryPoemById(id)
        if (poem == null) {
            finish()          // 查不到，直接关掉
            return
        }

        // ③ 查到了就一个个填进去
        findViewById<TextView>(R.id.tvDetailTitle).text = poem.title
        findViewById<TextView>(R.id.tvDetailAuthor).text = "${poem.author} · ${poem.dynasty}"
        findViewById<TextView>(R.id.tvDetailMeta).text = "${poem.format} · ${poem.collection}"
        findViewById<TextView>(R.id.tvDetailContent).text = poem.content
        findViewById<TextView>(R.id.tvDetailNotes).text = poem.notes
        findViewById<TextView>(R.id.tvDetailTranslate).text = poem.translate
        findViewById<TextView>(R.id.tvDetailAppreciation).text = poem.appreciation
        findViewById<TextView>(R.id.tvDetailAuthorBio).text = poem.authorBio
        //多加一个换行，看起来更舒服
        findViewById<TextView>(R.id.tvDetailAppreciation).text =
            poem.appreciation.replace("\n", "\n\n")
        findViewById<TextView>(R.id.tvDetailTranslate).text =
            poem.translate.replace("\n", "\n\n")
        findViewById<TextView>(R.id.tvDetailNotes).text =
            poem.notes.replace("\n", "\n\n")
    }
}
