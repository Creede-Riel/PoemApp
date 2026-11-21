package com.example.poemapp.Ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.R
import com.example.poemapp.Data.DBHelper
import com.google.android.material.bottomnavigation.BottomNavigationView


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

        val navBar=findViewById<BottomNavigationView>(R.id.navBar)
        //点击底部导航栏切换不同的fragment
        navBar.setOnItemSelectedListener{item ->

            when (item.itemId){
                R.id.tabHome ->
                    supportFragmentManager.beginTransaction().
                    replace(R.id.fragmentContainer, HomeFragment()).
                    commit()

                R.id.tabFavorite ->
                    supportFragmentManager.beginTransaction().
                    replace(R.id.fragmentContainer, FavoriteFragment()).
                    commit()

                R.id.tabProfile ->
                    supportFragmentManager.beginTransaction().
                    replace(R.id.fragmentContainer, ProfileFragment()).
                    commit()
            }
            true
            //要记得返回true，不然点击tab会没有反应
        }
    }
}
