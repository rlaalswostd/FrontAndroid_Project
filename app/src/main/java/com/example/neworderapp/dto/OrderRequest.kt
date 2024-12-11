package com.example.neworderapp.dto

data class OrderRequest(
    val storeId: String,
    val tableNumber: String,  // 테이블 ID
    val items: List<OrderItemRequest>  // 주문 항목 리스트
)


data class OrderItemRequest(
    val menuId: Long,  // 메뉴 ID
    val quantity: Int,  // 수량
    val request: String?  // 요청 사항
)