package com.example.poemapp.UI

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = DBHelper(requireContext()).queryAllPoems()//调用DBhelper里面的函数把诗词数据交给list

        val rv = view.findViewById<RecyclerView>(R.id.rvPoems)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = PoemAdapter(list)
    }
}