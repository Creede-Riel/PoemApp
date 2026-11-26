package com.example.poemapp.Ui

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R

class Register : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        dbHelper = DBHelper(this)

        val etAccount = findViewById<EditText>(R.id.etAccount)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnLogin)
        //变换眼睛状态和密码可见状态
        //找到密码框之后设置眼睛的状态变量
        var passwordVisible = false
        // 现在密码是"隐藏"状态
        val ivToggle = findViewById<ImageView>(R.id.ivTogglePassword)
        findViewById<ImageView>(R.id.ivTogglePassword).setOnClickListener {
            passwordVisible = !passwordVisible        // 翻转状态

            if (passwordVisible) {
                // 显示明文
                etPassword.inputType = InputType.TYPE_CLASS_TEXT
                ivToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                // 恢复成圆点
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggle.setImageResource(R.drawable.ic_visibility_off)
            }

            // 光标挪到末尾（不然切换密码可见状态会自动跳回开头）
            etPassword.setSelection(etPassword.text.length)
        }


        btnRegister.setOnClickListener {
            val account = etAccount.text.toString().trim()      // 用户输入的账号
            val password = etPassword.text.toString().trim()    // 用户输入的密码

            // ① 非空
            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "账号或密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ② 账号长度（1–12 位）
            if (account.length > 12) {
                Toast.makeText(this, "账号不能超过 12 位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ③ 密码长度（至少 6 位）
            if (password.length < 6) {
                Toast.makeText(this, "密码不能小于六位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ④ 查重（用输入框里的账号，不是 SharedPreferences）
            if (dbHelper.isUserExists(account)) {
                Toast.makeText(this, "该账号已被注册，请换一个", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ⑤ 全部通过，才写入数据库（只写一次）
            dbHelper.register(account, password)

            // 注册成功 → 把账号密码存起来，回到登录页会自动填好
            getSharedPreferences("login_prefs", MODE_PRIVATE)
                .edit()
                .putString("savedAccount", account)
                .putString("savedPassword", password)
                .putBoolean("justRegistered", true)
                .apply()

            Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show()
            finish()    // 关闭本页，回到登录页
        }
    }
}
