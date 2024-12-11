package com.example.neworderapp.service

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path


interface OrderStatusService {
    @POST("/api/orders/update-status/{storeId}/{tableNumber}")
    fun updateOrderStatusToCompleted(
        @Path("storeId") storeId: String?,
        @Path("tableNumber") tableNumber: String?
    ):Call<Map<String, Any>>
}