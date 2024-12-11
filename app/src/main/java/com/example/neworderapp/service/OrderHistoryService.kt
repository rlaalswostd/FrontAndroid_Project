package com.example.neworderapp.service

import com.example.neworderapp.dto.OrderHistory
import com.example.neworderapp.dto.OrderedMenuResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface OrderHistoryService {

    // 주문 총금액, 최신 주문시간  livedata로 띄우는 api
    @GET("api/orderHistory/{storeId}/{tableNumber}")
     fun getOrderHistory(
        @Path("storeId") storeId: String,
        @Path("tableNumber") tableNumber: String
    ): Call<List<OrderHistory>>

    @GET("api/orders/ordered-menus/{storeId}/{tableNumber}")
    fun getOrderedMenus(
        @Path("storeId") storeId: String,
        @Path("tableNumber") tableNumber: String
    ): Call<List<OrderedMenuResponse>> //dto 응답

}