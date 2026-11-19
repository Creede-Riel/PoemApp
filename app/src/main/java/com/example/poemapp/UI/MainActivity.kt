package com.example.poemapp.UI

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.R
import com.example.poemapp.Data.DBHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val dbHelper = DBHelper(this)
        //诗词数目不为零就跳过导入诗词数据
        if (dbHelper.poemCount() == 0) dbHelper.importPoems(this)

        // 只在第一次创建时加载，避免旋转屏幕后重复添加
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()

        }
    }
}
