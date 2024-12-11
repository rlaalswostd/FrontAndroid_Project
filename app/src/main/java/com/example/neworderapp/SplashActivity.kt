package com.example.neworderapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 상태바와 네비게이션 바 숨기기
        val window: Window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        // 네비게이션 바도 숨기려면 다음 코드도 추가
        val decorView: View = window.getDecorView()
        val uiOptions =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        setContentView(R.layout.activity_splash)

        // 일정 시간 후에 MainActivity로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, OrderFirstActivity::class.java)
            startActivity(intent)
            finish() // 스플래시 액티비티 종료
        }, 2000) // 3초 지연
    }
}
