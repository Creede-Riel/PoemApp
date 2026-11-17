package com.example.poemapp.register

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.R
import com.example.poemapp.database.DBHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Register : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        dbHelper= DBHelper(this)
        val etAccount = findViewById<EditText>(R.id.etAccount)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val account = etAccount.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "账号或密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "密码不能小于六位数字", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dbHelper.register(account,password)//账号密码合适，存入数据库
            Toast.makeText(this,"注册成功，请登录", Toast.LENGTH_SHORT).show()
            finish()//销毁当前页面，回到登录页面

        }
    }

}