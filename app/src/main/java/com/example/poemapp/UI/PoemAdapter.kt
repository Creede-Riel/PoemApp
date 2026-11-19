package com.example.poemapp.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poemapp.R
import com.example.poemapp.model.Poem

//建适配器
class PoemAdapter(private val list: List<Poem>,
                  private val onClick: (Poem) -> Unit   ) :
    RecyclerView.Adapter<PoemAdapter.PoemViewHolder>() {
    //加了一个回调函数，会把数据传给使用该适配器的fragment
    // ① 一共几条
    override fun getItemCount(): Int = list.size

    // ② 卡片长什么样（不够用时才调）
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poem, parent, false)
        //把item_poem布局吹成可视的view
        return PoemViewHolder(view)
    }

    // ③ 第 position 张填什么数据
    override fun onBindViewHolder(holder: PoemViewHolder, position: Int) {
        val poem = list[position]
        //取出第position位的数据
        //把数据塞进去
        holder.tvTitle.text = poem.title
        holder.tvAuthor.text = "${poem.author} · ${poem.dynasty}"
        holder.tvContent.text = poem.famousLine.ifEmpty { poem.content }//没有名句就显示原文
        holder.itemView.setOnClickListener {
            onClick(poem)
        }
        //在塞数据的同时加入监听器，点击了诗词卡片，适配器就会把这首诗的数据传给HomeFragment
    }

    // ViewHolder：存好 findViewById 的结果，避免每次绑定都重新找，提前执行findviewbyid，把控件提前找到存起来
    class PoemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
    }


}