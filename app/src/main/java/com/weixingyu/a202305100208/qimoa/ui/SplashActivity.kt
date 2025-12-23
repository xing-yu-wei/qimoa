package com.weixingyu.a202305100208.qimoa.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.weixingyu.a202305100208.qimoa.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使用 ViewBinding 加载界面
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 延迟 2000 毫秒 (2秒) 后跳转
        Handler(Looper.getMainLooper()).postDelayed({
            // 跳转到 MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // 关闭当前页面，这样按返回键就不会回到启动页了
            finish()
        }, 2000)
    }
}