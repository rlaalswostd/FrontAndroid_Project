package com.example.neworderapp.dto

data class OrderedMenuResponse(
    val menuName: String,
    val menuPrice: Int,
    val orderQuantity: Int,
    val tableNumber: String,
    val storeId: String

)
