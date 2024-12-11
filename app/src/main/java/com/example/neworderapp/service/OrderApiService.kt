package com.example.neworderapp.service

import com.example.neworderapp.dto.OrderRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApiService {


    @POST("/api/orderitems/create")
    fun createOrder(@Body orderRequest: OrderRequest): Call<Void>
}