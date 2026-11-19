package com.example.poemapp.UI


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.R
import com.example.poemapp.Data.DBHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val dbHelper= DBHelper(this)
        Log.d("PoemApp", "第一条：${dbHelper.queryAllPoems().firstOrNull()?.title}")
    }
}
