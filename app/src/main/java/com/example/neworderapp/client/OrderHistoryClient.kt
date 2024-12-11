package com.example.neworderapp.client

import com.example.neworderapp.MyApp
import com.example.neworderapp.R
import com.example.neworderapp.service.MenuApiService
import com.example.neworderapp.service.OrderHistoryService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OrderHistoryClient {

    private val BASE_URL: String
        get() = MyApp.applicationContext().getString(R.string.base_url)

    // Retrofit 객체를 생성
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Retrofit 인스턴스를 통해 MenuApiService 생성
    val orderHistoryService:  OrderHistoryService = retrofit.create(OrderHistoryService::class.java)


}