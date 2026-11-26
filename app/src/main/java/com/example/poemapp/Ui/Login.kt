package com.example.poemapp.Ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poemapp.R
import com.example.poemapp.Data.DBHelper

class Login : AppCompatActivity() {
    private lateinit var dbHelper: DBHelper

    // 界面是否已经加载好（"已登录跳过"那条路不会加载界面，onResume 里要用它判断）
    private var contentReady = false

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
        contentReady = true
        dbHelper = DBHelper(this)
        val etAccount=findViewById<EditText>(R.id.etAccount)
        val etPassword=findViewById<EditText>(R.id.etPassword)

        val btnLogin=findViewById<Button>(R.id.btnLogin)
        val cbRememberPassword=findViewById<CheckBox>(R.id.cbRememberPassword)

        fillSavedAccount(etAccount, etPassword, cbRememberPassword)   // 首次进入先填一次
        btnLogin.setOnClickListener {
            val account=etAccount.text.toString().trim()
            val password=etPassword.text.toString().trim()
            if (account.isEmpty()||password.isEmpty()){
                Toast.makeText(this,"账号或密码不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (account.length>12){
                Toast.makeText(this,"账号不能大于12位", Toast.LENGTH_SHORT).show()
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


        val toGoToRegister=findViewById<TextView>(R.id.toGoToRegister)
        toGoToRegister.setOnClickListener {
            val intent= Intent(this, Register::class.java)
            startActivity(intent)
        //跳转到注册页的逻辑
        }

    }

    // 每次页面变可见都执行（从注册页返回时也会走这里）
    override fun onResume() {
        super.onResume()
        if (!contentReady) return      // 走"已登录跳过"那条路时没有界面，直接返回

        val etAccount = findViewById<EditText>(R.id.etAccount)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val cbRememberPassword = findViewById<CheckBox>(R.id.cbRememberPassword)
        fillSavedAccount(etAccount, etPassword, cbRememberPassword)
    }

    // 把记住的账号密码回填到输入框（只在输入框为空时填，不覆盖用户正在打的字）
    private fun fillSavedAccount(
        etAccount: EditText,
        etPassword: EditText,
        cbRememberPassword: CheckBox
    ) {
        val sp = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val savedAccount = sp.getString("savedAccount", "") ?: ""
        val savedPassword = sp.getString("savedPassword", "") ?: ""

        // 刚从注册页回来 → 强制填成刚注册的账号密码
        if (sp.getBoolean("justRegistered", false)) {
            etAccount.setText(savedAccount)
            etPassword.setText(savedPassword)
            sp.edit().remove("justRegistered").apply()
            cbRememberPassword.isChecked = true
            return
        }

        if (etAccount.text.isNullOrEmpty() && savedAccount.isNotEmpty()) {
            etAccount.setText(savedAccount)
        }
        if (etPassword.text.isNullOrEmpty() && savedPassword.isNotEmpty()) {
            etPassword.setText(savedPassword)
        }
        cbRememberPassword.isChecked = savedAccount.isNotEmpty() && savedPassword.isNotEmpty()
    }
}