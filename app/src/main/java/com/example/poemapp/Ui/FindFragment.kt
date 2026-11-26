package com.example.poemapp.Ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R

class FindFragment : Fragment(R.layout.fragment_find) {

    private lateinit var db: DBHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHelper(requireContext())

        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        // 输入框内容变化 → 立刻搜索（边打边搜）
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                doSearch(s.toString().trim())
            }
        })
    }

    // 执行搜索并刷新界面
    private fun doSearch(keyword: String) {
        val v = view ?: return
        val rv = v.findViewById<RecyclerView>(R.id.rvSearch)
        val tvEmpty = v.findViewById<TextView>(R.id.tvEmpty)

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

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = PoemAdapter(list) { poem ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("poemId", poem.id)
            startActivity(intent)
        }
    }
}