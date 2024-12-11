package com.example.neworderapp.data

import java.math.BigDecimal

data class OrderItem(
    val orderItemId: Long,
    val orderId: Long,
    val menuId: Long,
    val menuName: String ,
    val quantity: Int ,
    val unitPrice: BigDecimal,
    val request: String? = null
)