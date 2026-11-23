package com.example.poemapp.Ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)    //加载布局

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    //每次进入历史界面就重新加载
    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val sp = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val username = sp.getString("currentUser", "") ?: ""

        val list = DBHelper(this).queryHistory(username)

        val rv = findViewById<RecyclerView>(R.id.rvHistory)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        if (list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = PoemAdapter(list) { poem ->             // ④ 适配器类名
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("poemId", poem.id)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.btnClear).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("清空浏览历史")
                .setMessage("确定要清空所有浏览记录吗？")
                .setPositiveButton("清空") { _, _ ->
                    DBHelper(this).clearHistory(username)
                    //上面已经把数据库里面的历史记录清空了，要重新刷新一下界面，不然界面会保留清空之前的样子
                    refreshList()
                    Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }


}