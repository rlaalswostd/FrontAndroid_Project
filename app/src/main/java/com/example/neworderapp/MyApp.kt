package com.example.neworderapp

import android.app.Application
import android.content.Context
import com.example.neworderapp.viewmodel.MenuViewModel

class MyApp : Application() {
    companion object {
        private lateinit var instance: MyApp

        // 애플리케이션 컨텍스트 반환
        fun applicationContext(): Context {
            return instance.applicationContext
        }

        // 애플리케이션 인스턴스 반환
        fun getInstance(): MyApp {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // 앱 초기화 코드 (ViewModel은 여기서 처리하지 않음)
    }
}