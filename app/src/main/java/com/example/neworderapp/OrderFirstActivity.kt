package com.example.neworderapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.neworderapp.databinding.OrderFrontFirstBinding

class OrderFirstActivity : AppCompatActivity() {

    private lateinit var binding: OrderFrontFirstBinding

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

        binding = OrderFrontFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // '주문하기' 버튼 클릭 시 호출되는 메서드
    fun onFinalButtonClick(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }
}