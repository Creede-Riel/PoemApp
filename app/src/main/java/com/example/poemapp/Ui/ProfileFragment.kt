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
import android.widget.GridLayout
import android.widget.ImageView
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


        // 显示头像
        val avatarName = db.getAvatar(username)          // "avatar_3" 或 ""
        val name = if (avatarName.isEmpty()) "avatar_1" else avatarName
        val resId = resources.getIdentifier(name, "drawable", requireContext().packageName)
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        ivAvatar.setImageResource(if (resId != 0) resId else R.drawable.avatar_1)

        view.findViewById<androidx.cardview.widget.CardView>(R.id.cardAvatar).setOnClickListener {
            val avatarNames = listOf(
                "avatar_1", "avatar_2", "avatar_3",
                "avatar_4", "avatar_5", "avatar_6"
            )
            val density = resources.displayMetrics.density
            val size = (76 * density).toInt()
            val gap = (10 * density).toInt()
            val stroke = (3 * density).toInt()

            var selectedName = if (avatarName.isEmpty()) "avatar_1" else avatarName
            val cards = mutableListOf<com.google.android.material.card.MaterialCardView>()

            val grid = GridLayout(requireContext())
            grid.columnCount = 3
            grid.setPadding(gap, gap, gap, gap)

            var dialog: androidx.appcompat.app.AlertDialog? = null

            // 统一刷新描边：只有选中的那个画框
            fun refreshStroke() {
                cards.forEachIndexed { index, card ->
                    if (avatarNames[index] == selectedName) {
                        card.strokeWidth = stroke
                        card.strokeColor = android.graphics.Color.parseColor("#7CB342")
                    } else {
                        card.strokeWidth = 0
                    }
                }
            }

            avatarNames.forEach { name ->
                val card = com.google.android.material.card.MaterialCardView(requireContext())
                val lp = GridLayout.LayoutParams()
                lp.width = size
                lp.height = size
                lp.setMargins(gap, gap, gap, gap)
                card.layoutParams = lp
                card.radius = size / 2f
                card.cardElevation = 0f
                card.strokeWidth = 0

                val iv = ImageView(requireContext())
                iv.layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                iv.scaleType = ImageView.ScaleType.CENTER_CROP
                val rid = resources.getIdentifier(name, "drawable", requireContext().packageName)
                if (rid != 0) iv.setImageResource(rid)

                iv.setOnClickListener {
                    selectedName = name     // 只记下，不写库
                    refreshStroke()
                }

                card.addView(iv)
                grid.addView(card)
                cards.add(card)
            }

            refreshStroke()   // 进来先给当前头像画框

            // 直接换成 AlertDialog（带确定/取消）
            dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("选择头像")
                .setView(grid)
                .setPositiveButton("确定") { _, _ ->
                    db.updateAvatar(username, selectedName)      // 现在才写库
                    val rid = resources.getIdentifier(selectedName, "drawable", requireContext().packageName)
                    ivAvatar.setImageResource(if (rid != 0) rid else R.drawable.avatar_1)
                    Toast.makeText(requireContext(), "头像已更换", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("取消", null)
                .create()
            dialog.show()
        }


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


