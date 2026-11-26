package com.example.poemapp.Ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.poemapp.Data.DBHelper
import com.example.poemapp.R



class ProfileFragment : Fragment(R.layout.fragment_profile){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //自动填入登录时的账号
        val sp = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvAccount = view.findViewById<TextView>(R.id.tvAccount)
        val db = DBHelper(requireContext())
        val username = sp.getString("currentUser", "") ?: ""
        val nickname = db.getNickname(username)          // ① 查昵称的方法
        tvUsername.text = if (nickname.isEmpty()) username else nickname // ② 空就用账号
        tvAccount.text = "账号 · ${sp.getString("currentUser", "")}"
        //退出登录键加弹窗防误触
        val btnLogout = view.findViewById<Button>(R.id.btnLogOut)
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("退出") { _, _ ->
                    sp.edit().putBoolean("isLogin", false).apply()
                    startActivity(Intent(requireContext(), Login::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("取消", null)
                .show()
             }

        //点击历史记录跳转到浏览记录界面
        view.findViewById<TextView>(R.id.tvHistory).setOnClickListener {
            startActivity(Intent(requireContext(), HistoryActivity::class.java))
        }

        view.findViewById<TextView>(R.id.tvChangePassword).setOnClickListener {
            val sp = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val username = sp.getString("currentUser", "") ?: ""

            // ① 动态创建一个输入框
            val input = EditText(requireContext())
            input.hint = "请输入新密码（至少六位）"
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            // ② 包一层容器，给输入框加左右边距（不加会贴着弹窗边缘）
            val container = LinearLayout(requireContext())
            container.setPadding(48, 24, 48, 0)
            container.addView(input)

            // ③ 弹窗
            AlertDialog.Builder(requireContext())
                .setTitle("修改密码")
                .setView(container)
                .setPositiveButton("确定") { _, _ ->
                    val newPassword = input.text.toString().trim()

                    // ④ 校验放在这里 —— 用户已经输入完了
                    if (newPassword.isEmpty()) {
                        Toast.makeText(requireContext(), "密码不能为空", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (newPassword.length < 6) {
                        Toast.makeText(requireContext(), "密码不能小于六位", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (DBHelper(requireContext()).checkLogin(username, newPassword)) {
                        Toast.makeText(requireContext(), "新密码不能与旧密码相同", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // ⑤ 改数据库
                    DBHelper(requireContext()).updatePassword(username, newPassword)
                    Toast.makeText(requireContext(), "密码已修改，下次登录生效", Toast.LENGTH_SHORT).show()
                    // 如果之前勾了"记住密码"，把存的密码也更新掉，就不会出现登录页自动填入旧密码的bug
                    sp.edit().putString("savedPassword", newPassword).apply()
                }
                .setNegativeButton("取消", null)
                .show()
        }

        //修改昵称
        view.findViewById<TextView>(R.id.tvChangeNickname).setOnClickListener {
            val sp = requireContext().getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
            val username = sp.getString("currentUser", "") ?: ""

            // ① 动态创建一个输入框
            val input = EditText(requireContext())
            input.hint = "请输入新昵称（不得大于12个字）"

            // ② 包一层容器，给输入框加左右边距（不加会贴着弹窗边缘）
            val container = LinearLayout(requireContext())
            container.setPadding(48, 24, 48, 0)
            container.addView(input)

            // ③ 弹窗
            AlertDialog.Builder(requireContext())
                .setTitle("修改昵称")
                .setView(container)
                .setPositiveButton("确定") { _, _ ->
                    val newNickname = input.text.toString().trim()

                    // ④ 校验放在这里 —— 用户已经输入完了
                    if (newNickname.isEmpty()) {
                        Toast.makeText(requireContext(), "昵称不能为空", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (newNickname.length > 9) {
                        Toast.makeText(requireContext(), "昵称不得大于12个字", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // ⑤ 改数据库
                    DBHelper(requireContext()).updateNickname(username, newNickname)
                    Toast.makeText(requireContext(), "昵称修改成功", Toast.LENGTH_SHORT).show()
                    tvUsername.text = newNickname
                }
                .setNegativeButton("取消", null)
                .show()
        }
        //点击关于弹出app相关信息
        view.findViewById<TextView>(R.id.tvAbout).setOnClickListener {
            val versionName =  requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
            AlertDialog.Builder(requireContext())
                .setTitle("关于 PoemApp")
                .setMessage(
                    """
            版本 $versionName

            一款古诗词学习应用：
            浏览 · 朗读 · 收藏 · 笔记

            诗词数据来自开源项目
            chinese-poetry-annotated（MIT 协议）

            2026 年绘蓝杯科技竞赛 B 组参赛作品
            """.trimIndent()
                )
                .setPositiveButton("知道了", null)
                .show()
        }
        //更新个人主页的版本号
        val tvVersion = view.findViewById<TextView>(R.id.tvVersion)
        val versionName = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionName
        tvVersion.text = "PoemApp v$versionName"
         }
    }


