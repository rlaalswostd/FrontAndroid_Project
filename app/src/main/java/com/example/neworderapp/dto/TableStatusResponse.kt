package com.example.neworderapp.dto

data class TableStatusResponse(val tableExists: Boolean,
                               val occupied: Boolean //1이면 사용가능
)
