package com.example.neworderapp.data


data class Menu(
    val id: Long,
    val categoryId: Int,
    val name: String,
    val price: Int,
    val isAvailable: Int,
    val storeId: String

)
