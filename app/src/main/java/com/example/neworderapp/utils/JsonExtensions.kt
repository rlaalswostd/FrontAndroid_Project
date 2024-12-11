package com.example.neworderapp.utils  // 유틸리티 파일의 패키지 경로

import com.google.gson.JsonElement

fun JsonElement?.asIntOrDefault(defaultValue: Int): Int {
    return when {
        this == null || this.isJsonNull -> defaultValue
        this.isJsonPrimitive -> {
            val primitive = this.asJsonPrimitive
            when {
                primitive.isNumber -> primitive.asInt
                primitive.isString -> primitive.asString.toIntOrNull() ?: defaultValue
                else -> defaultValue
            }
        }
        else -> defaultValue
    }
}

fun JsonElement?.asBooleanOrDefault(defaultValue: Boolean): Boolean {
    return when {
        this == null || this.isJsonNull -> defaultValue
        this.isJsonPrimitive -> {
            val primitive = this.asJsonPrimitive
            when {
                primitive.isBoolean -> primitive.asBoolean
                primitive.isNumber -> primitive.asInt != 0
                primitive.isString -> primitive.asString.toBoolean()
                else -> defaultValue
            }
        }
        else -> defaultValue
    }
}