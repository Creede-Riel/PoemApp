package com.example.poemapp.UI

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R
import java.util.Calendar

//先加载布局，直接把布局传进来
class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = DBHelper(requireContext()).queryAllPoems()//调用DBhelper里面的函数把诗词数据交给list
        //每日精选诗词卡片，一天一换
        val day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)   // 今天是今年第几天，比如 325
        val daily = list[day % list.size]                            // 325 % 611 = 325，取第 325 首
        val tvDailyTitle = view.findViewById<TextView>(R.id.tvDailyTitle)
        val tvDailyAuthor = view.findViewById<TextView>(R.id.tvDailyAuthor)
        val tvDailyLine = view.findViewById<TextView>(R.id.tvDailyLine)

        tvDailyTitle.text = daily.title
        tvDailyAuthor.text = "${daily.author} · ${daily.dynasty}"
        tvDailyLine.text = daily.famousLine.ifEmpty { daily.content }

        // 点卡片进详情
        view.findViewById<View>(R.id.cardDaily).setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("poemId", daily.id)
            startActivity(intent)
        }

        val rv = view.findViewById<RecyclerView>(R.id.rvPoems)//找到滑动列表
        rv.layoutManager = LinearLayoutManager(requireContext())//定义布局管理器进行排版
        //绑定适配器，fragment获取到适配器传过来的poem后，执行逻辑：跳转到详情页
        rv.adapter = PoemAdapter(list){poem->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("poemId", poem.id)
            startActivity(intent)
        }

    }
}
//这里只是一个装滑动列表的fragment