package com.example.poemapp.Ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
        tvUsername.text = sp.getString("currentUser", "")
        val tvAccount = view.findViewById<TextView>(R.id.tvAccount)
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

         }
    }


