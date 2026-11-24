package com.example.poemapp.Ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R

class SearchActivity : AppCompatActivity() {

    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        db = DBHelper(this)

        // 返回
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        val etSearch = findViewById<EditText>(R.id.etSearch)
// 自动聚焦 + 弹键盘
        etSearch.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT)

// 如果从别处带着关键词进来（详情页标签、adb 测试）
        val preKeyword = intent.getStringExtra("keyword")
        if (!preKeyword.isNullOrEmpty()) {
            etSearch.setText(preKeyword)
            etSearch.setSelection(preKeyword.length)     // 光标放末尾
        }

        // 输入框内容变化 → 立刻搜索（边打边搜）
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                doSearch(s.toString().trim())
            }
        })

        val keyword = intent.getStringExtra("keyword")
        if (!keyword.isNullOrEmpty()) {
            etSearch.setText(keyword)
            etSearch.setSelection(keyword.length)
        }
    }

    // 执行搜索并刷新界面
    private fun doSearch(keyword: String) {
        val rv = findViewById<RecyclerView>(R.id.rvSearch)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        // 没输入 → 两个都藏起来（空白页）
        if (keyword.isEmpty()) {
            rv.visibility = View.GONE
            tvEmpty.visibility = View.GONE
            return
        }

        val list = db.queryPoemsByKeyword(keyword)

        //  没结果/有结果
        if (list.isEmpty()) {
            tvEmpty.text = "没有找到「$keyword」相关的诗词"
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = PoemAdapter(list) { poem ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("poemId", poem.id)
            startActivity(intent)
        }
    }
}