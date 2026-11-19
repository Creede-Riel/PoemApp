package com.example.poemapp.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.R
import com.example.poemapp.Data.DBHelper

class Login : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sp = getSharedPreferences("login_prefs", MODE_PRIVATE)
        if (sp.getBoolean("isLogin", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        //检查是否登录过，登录过就直接进入主界面，没有登录过就显示登录界面
        setContentView(R.layout.login)
        dbHelper = DBHelper(this)
        val etAccount=findViewById<EditText>(R.id.etAccount)
        val etPassword=findViewById<EditText>(R.id.etPassword)
        val btnLogin=findViewById<Button>(R.id.btnLogin)
        val cbRememberPassword=findViewById<CheckBox>(R.id.cbRememberPassword)
        cbRememberPassword.isChecked = !sp.getString("savedAccount", "").isNullOrEmpty()
        // 之前记住过账号 → 复选框默认勾上
        etAccount.setText(sp.getString("savedAccount", ""))
        etPassword.setText(sp.getString("savedPassword", ""))
        //勾选了就把记住的账号密码回填，否则默认填入空白
        btnLogin.setOnClickListener {
            val account=etAccount.text.toString().trim()
            val password=etPassword.text.toString().trim()
            if (account.isEmpty()||password.isEmpty()){
                Toast.makeText(this,"账号或密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length<6){
                Toast.makeText(this,"密码不能小于六位数字", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //检查账号密码是否与注册时一致，一致就储存账号密码并跳转到主界面
            if (dbHelper.checkLogin(account, password)) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()

                sp.edit()
                    .putBoolean("isLogin", true)//留下已登录标志
                    .putString("currentUser", account)
                    .apply()

                if (cbRememberPassword.isChecked) {
                    sp.edit().putString("savedAccount", account).putString("savedPassword", password).apply()
                } else {
                    sp.edit().remove("savedAccount").remove("savedPassword").apply()
                }// 勾了 → 存起来；没勾 → 清掉之前存的

                val intent= Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show()
            }


        }

        val toGoToRegister=findViewById<TextView>(R.id.toGoToRegister)
        toGoToRegister.setOnClickListener {
            val intent= Intent(this, Register::class.java)
            startActivity(intent)
        //跳转到注册页的逻辑
        }

    }
}