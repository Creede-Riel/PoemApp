package com.example.poemapp.Ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    //留一个检查诗词收藏状态的变量
    private var favorited = false
    private var tts: TextToSpeech? = null
    //创建tts是发起初始化
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details)

        tts = TextToSpeech(this) { status ->
            //这个是初始化回调，初始化需要时间，系统初始化完成后会把状态结果传回来
            if (status == TextToSpeech.SUCCESS) {
                //初始化成功才会把朗读语音设置成中文，初始化失败就不设置语音
                tts?.language = Locale.CHINESE
                Log.d("PoemApp", "中文支持度=${tts?.isLanguageAvailable(Locale.CHINESE)}")
            }
        }

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

        findViewById<Button>(R.id.btnSpeak).setOnClickListener {
            tts?.speak(
                poem.content,
                //朗读原文
                TextToSpeech.QUEUE_FLUSH,
                //这个是直接打断旧的朗读开始新的朗读
                null,
                null
            )
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

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()          // 关掉自己，回到列表
        }

    //下面这段检索收藏表决定诗词详情页的收藏键的状态
        // ① 拿当前用户
        val username = getSharedPreferences("login_prefs", MODE_PRIVATE)
            .getString("currentUser", "") ?: ""

        // ② 拿到数据库对象（只要一次）
        val dbHelper = DBHelper(this)

        //插一个，每次进入详情页就加浏览历史
        dbHelper.addHistory(username,id)

        // ③ 查这一首有没有被收藏过
        favorited = dbHelper.isFavorite(username, id)

        // ④ 按状态设置按钮文字
        val btnFavorite = findViewById<Button>(R.id.btnFavorite)
        btnFavorite.text = if (favorited) "★ 已收藏" else "☆ 收藏"


        //下面这段调用add和delate，在点击收藏键的时候加入或者取消收藏
        btnFavorite.setOnClickListener {
            if (favorited) {
                dbHelper.removeFavorite(username, id)     // 取消收藏
                favorited = false
                btnFavorite.text = "☆ 收藏"
            } else {
                dbHelper.addFavorite(username, id)     // 添加收藏
                favorited = true
                btnFavorite.text = "★ 已收藏"
            }
        }

//把诗词的标签拆成小部分放进chip里
        val tags = poem.theme.split("、").filter { it.isNotBlank() }

        val chipTags = findViewById<ChipGroup>(R.id.chipTags)
        val tvTagLabel = findViewById<TextView>(R.id.tvTagLabel)
        chipTags.removeAllViews()

        if (tags.isEmpty()) {
            chipTags.visibility = View.GONE
            tvTagLabel.visibility = View.GONE
        } else {
            chipTags.visibility = View.VISIBLE
            tvTagLabel.visibility = View.VISIBLE

            tags.forEach { tag ->
                val chip = Chip(this)
                chip.text = tag
                chip.isCheckable = false
                chip.isClickable = false
                chip.textSize = 12f
                chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#EFEFEF"))
                chip.setTextColor(Color.parseColor("#555555"))
                chip.setPadding(8, 0, 8, 0)
                chip.chipStrokeWidth = 0f
                chipTags.addView(chip)
            }
        }
        chipTags.visibility = if (tags.isEmpty()) View.GONE else View.VISIBLE
            }

    //释放资源
        override fun onDestroy() {
            tts?.stop()
        //立即停止朗读
            tts?.shutdown()
        //释放引擎
            super.onDestroy()
    }
        }



