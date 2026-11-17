package com.example.poemapp.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.poemapp.R
import com.example.poemapp.database.DBHelper
import com.example.poemapp.mainactivity.MainActivity
import com.example.poemapp.register.Register

class Login : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sp = getSharedPreferences("login_prefs", MODE_PRIVATE)
        if (sp.getBoolean("isLogin", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }//检查是否登录过，登录过就直接进入主界面，没有登录过就显示登录界面
        setContentView(R.layout.login)
        dbHelper = DBHelper(this)
        val etAccount=findViewById<EditText>(R.id.etAccount)
        val etPassword=findViewById<EditText>(R.id.etPassword)
        val btnLogin=findViewById<Button>(R.id.btnLogin)
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
            if (dbHelper.checkLogin(account, password)) {//检查账号密码是否与注册时一致
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                val intent= Intent(this, MainActivity::class.java)
                startActivity(intent)
                val sp = getSharedPreferences("login_prefs", MODE_PRIVATE)//留下已登录标志
                sp.edit()
                    .putBoolean("isLogin", true)
                    .putString("currentUser", account)
                    .apply()
            } else {
                Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show()
            }


        }

        val toGoToRegister=findViewById<TextView>(R.id.toGoToRegister)
        toGoToRegister.setOnClickListener {
            val intent= Intent(this, Register::class.java)
            startActivity(intent)
        }

    }
}