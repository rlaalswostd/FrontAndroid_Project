package com.example.neworderapp.client

import android.util.Log
import com.example.neworderapp.MyApp
import com.example.neworderapp.R
import com.example.neworderapp.data.Menu
import com.example.neworderapp.service.MenuApiService
import com.example.neworderapp.utils.asIntOrDefault  // 확장 함수 import
import com.example.neworderapp.utils.asBooleanOrDefault  // 확장 함수 import
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.reflect.Type


object MenuClient {
    private val BASE_URL: String
        get() = MyApp.applicationContext().getString(R.string.base_url)

    // OkHttp 클라이언트 설정
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor { message ->
            if (message.length < 4000) {
                Log.d("OkHttp", message)
            } else {
                // 긴 메시지는 나눠서 로깅
                val chunkSize = 4000
                for (i in 0 until message.length step chunkSize) {
                    val end = minOf(message.length, i + chunkSize)
                    Log.d("OkHttp", message.substring(i, end))
                }
            }
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()


    // Gson 설정
    private val gson = GsonBuilder()
        .serializeNulls() // Null 값도 직렬화
        .setLenient()
        .disableHtmlEscaping()
        .registerTypeAdapter(Menu::class.java, object : JsonDeserializer<Menu> {
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Menu? {
                return try {
                    json.asJsonObject.run {
                        Menu(
                            id = get("id")?.asLong ?: 0L,
                            categoryId = get("categoryId")?.asIntOrDefault(0) ?: 0,
                            name = get("name")?.asString ?: "",
                            price = get("price")?.asIntOrDefault(0) ?: 0,
                            isAvailable = if (get("isAvailable")?.asBooleanOrDefault(false) == true) 1 else 0,
                            storeId = get("storeId")?.asString ?: ""
                        )

                    }
                } catch (e: Exception) {
                    Log.e("Gson", "Menu parsing error", e)
                    null
                }
            }
        })
        .create()  // Retrofit 객체 생성

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Retrofit 인스턴스를 통해 MenuApiService 생성
    val menuApiService: MenuApiService = retrofit.create(MenuApiService::class.java)
}

// 확장 함수 정의
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