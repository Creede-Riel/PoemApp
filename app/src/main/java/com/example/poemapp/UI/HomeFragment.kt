package com.example.poemapp.UI

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R

//先加载布局，直接把布局传进来
class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = DBHelper(requireContext()).queryAllPoems()//调用DBhelper里面的函数把诗词数据交给list

        val rv = view.findViewById<RecyclerView>(R.id.rvPoems)//找到滑动列表
        rv.layoutManager = LinearLayoutManager(requireContext())//定义布局管理器进行排版
        //绑定适配器
        rv.adapter = PoemAdapter(list){poem->
           Log.d("PoemApp","点击了诗词${poem.title}")
        }
    }
}
//这里只是一个装滑动列表的fragment