package com.example.poemapp.login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.poemapp.R

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

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
            else{
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
            }
        }

    }
}