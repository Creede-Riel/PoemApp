package com.example.poemapp.Ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    // 每次页面重新可见都会执行：第一次显示、从详情页返回、App 切回来
    override fun onResume() {
        super.onResume()
        refreshList()
    }

    // 把"查数据 + 刷新界面"打包成一个方法，方便反复调用
    private fun refreshList() {
        // 视图还没创建好就直接返回（防止空指针）
        val v = view ?: return

        // ① 当前用户
        val sp = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sp.getString("currentUser", "") ?: ""

        // ② 查这个用户的收藏
        val list = DBHelper(requireContext()).queryFavorites(username)

        // ③ 拿到两个控件
        val rv = v.findViewById<RecyclerView>(R.id.rvFavorites)
        val tvEmpty = v.findViewById<TextView>(R.id.tvEmpty)

        // ④ 空列表 → 显示提示文字，隐藏列表；有数据 → 反过来
        if (list.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }

        // ⑤ 装配列表（点击进详情）
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = PoemAdapter(list) { poem ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("poemId", poem.id)
            startActivity(intent)
        }
    }
}
