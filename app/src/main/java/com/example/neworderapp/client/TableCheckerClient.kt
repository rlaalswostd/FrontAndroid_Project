package com.example.neworderapp.client

import com.example.neworderapp.MyApp
import com.example.neworderapp.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TableCheckerClient {

    private val BASE_URL: String
        get() = MyApp.applicationContext().getString(R.string.base_url)

    val tableCheckerInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}