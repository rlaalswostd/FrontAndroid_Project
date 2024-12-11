package com.example.neworderapp.data

import java.math.BigDecimal
import java.util.Date


enum class OrderStatus {
    ORDERED, COMPLETED, CANCELLED
}

data class Orders(
    val orderId: Long,        // 주문번호
    val tableNumber: String,           // 테이블 ID
    val status: OrderStatus,    // 주문 상태 (Enum 타입)
    val totalAmount: BigDecimal, // 총금액
    val createdAt: Date,        // 생성일
    val orderItems: List<OrderItem> = emptyList()
)
