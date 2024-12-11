package com.example.neworderapp.client

import com.example.neworderapp.MyApp
import com.example.neworderapp.R
import com.example.neworderapp.service.OrderStatusService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OrderStatusClient {

    private val BASE_URL: String
        get() = MyApp.applicationContext().getString(R.string.base_url)

    // Retrofit 객체를 생성
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    companion object {
        val orderStatusService: OrderStatusService = Retrofit.Builder()
            .baseUrl(MyApp.applicationContext().getString(R.string.base_url)) // BASE_URL을 가져옴
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OrderStatusService::class.java)
    }


}