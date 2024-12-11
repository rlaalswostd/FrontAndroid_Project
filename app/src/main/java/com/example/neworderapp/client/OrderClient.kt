package com.example.neworderapp.client

import com.example.neworderapp.MyApp
import com.example.neworderapp.R
import com.example.neworderapp.service.OrderApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OrderClient {
    // Base URL을 앱의 리소스에서 가져옴


    companion object {
        private val BASE_URL: String
            get() = MyApp.applicationContext().getString(R.string.base_url)

        val orderApiService: OrderApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)  // 실제 API의 Base URL을 여기에 설정
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OrderApiService::class.java)

        }
    }
}