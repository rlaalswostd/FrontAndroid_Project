package com.example.neworderapp.service

import com.example.neworderapp.data.Category
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CategoryApiService {

    //매장별 CategoryId 목록 로드, 메인 화면 바로 가기

    @GET("api/categories/{storeId}")
    fun getCategoriesByStore(@Path("storeId") storeId: String): Call<List<Category>>  // 매장별 카테고리 목록
}