package com.example.neworderapp.dto

import java.math.BigDecimal

data class OrderHistory(
    val tableNumber: String,
    val lastOrderTime: String,  // 마지막 주문 시간
    val totalSum: BigDecimal,
    val storeId:String//총 주문 금액  status가 ordered 인
)
