package com.example.neworderapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.neworderapp.databinding.OrderBehindFinalBinding

class OrderBehindFinalActivity : AppCompatActivity() {

    // ViewBinding 객체를 사용하기 위해 lateinit으로 선언
    private lateinit var binding: OrderBehindFinalBinding

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


        // ViewBinding 객체 초기화
        binding = OrderBehindFinalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // VideoView 초기화
        val videoView = binding.backgroundVideoView

        // 동영상 파일 경로 설정
        val videoPath = "android.resource://${packageName}/${R.raw.orderbehindfinal}"
        val uri = Uri.parse(videoPath)
        videoView.setVideoURI(uri)

        // 비디오 재생 시작
        videoView.start()

        // 동영상 끝났을 때 처리
        videoView.setOnCompletionListener {
            val intent = Intent(this, OrderFirstActivity::class.java)
            startActivity(intent)  // MainActivity 시작
            finish()
        }

        // 버튼 클릭 시 동작 설정 (예: 액티비티 종료)
        binding.finalbtn.setOnClickListener {
            val intent = Intent(this, OrderFirstActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

