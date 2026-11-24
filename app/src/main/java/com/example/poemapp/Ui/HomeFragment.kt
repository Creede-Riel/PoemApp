package com.example.poemapp.Ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Calendar


//先加载布局，直接把布局传进来
class HomeFragment : Fragment(R.layout.fragment_home) {

    // null = 全部，否则是选中的朝代
    private var currentDynasty: String? = null

    // 数据库对象（refreshList 里也要用，所以放类里）
    private lateinit var db: DBHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBHelper(requireContext())

        val list = db.queryAllPoems()//调用DBhelper里面的函数把诗词数据交给list

        //根据不同的时间问候用户
        val sp = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sp.getString("currentUser", "") ?: ""

        val nickname = db.getNickname(username)
        val name = if (nickname.isEmpty()) "小友" else nickname

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 6  -> "凌晨好"
            hour < 12 -> "早上好"
            hour < 14 -> "中午好"
            hour < 18 -> "下午好"
            else      -> "晚上好"
        }

        view.findViewById<TextView>(R.id.tvGreeting).text = "$greeting，$name"

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

        // 先按"全部"显示一次列表
        refreshList()

        // ① 拿到 chip 容器
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup)

        // ② 先加"全部"
        val allChip = Chip(requireContext())
        allChip.text = "全部"
        allChip.isCheckable = true
        allChip.isChecked = true          // 默认选中
        chipGroup.addView(allChip)

        // ③ 每个朝代加一个
        db.queryAllDynasties().forEach { dynastyName ->
            val chip = Chip(requireContext())
            chip.text = dynastyName
            chip.isCheckable = true
            chipGroup.addView(chip)
        }

        // ④ 点击回调
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val checkedChip = group.findViewById<Chip>(checkedIds[0])
            val selected = checkedChip.text.toString()

            currentDynasty = if (selected == "全部") null else selected
            refreshList()
        }
        //跳转到搜索页
        view.findViewById<EditText>(R.id.etSearchBar).setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
    }

    // 查数据 + 刷新列表（点 chip 后会再次调用）
    private fun refreshList() {
        val v = view ?: return

        // 根据 currentDynasty 决定查什么
        val list = if (currentDynasty == null) {
            db.queryAllPoems()
        } else {
            db.queryPoemsByDynasty(currentDynasty!!)
        }

        val rv = v.findViewById<RecyclerView>(R.id.rvPoems)
        rv.layoutManager = LinearLayoutManager(requireContext())
        //绑定适配器，fragment获取到适配器传过来的poem后，执行逻辑：跳转到详情页
        rv.adapter = PoemAdapter(list) { poem ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("poemId", poem.id)
            startActivity(intent)
        }

        // 筛选时隐藏"今日推荐"
        v.findViewById<View>(R.id.cardDaily).visibility =
            if (currentDynasty == null) View.VISIBLE else View.GONE
    }

}


