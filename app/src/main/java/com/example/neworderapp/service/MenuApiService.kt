package com.example.neworderapp.service

import com.example.neworderapp.data.Category
import com.example.neworderapp.data.Menu
import com.example.neworderapp.data.Store
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MenuApiService {
    @GET("menus")
    fun getMenuList(): Call<List<Menu>>  //메뉴만 불러오기

    @GET("menus/categories")
    fun getCategoryList(): Call<List<Category>>

    @GET("menus/store/{storeId}/category/{categoryId}")  // 매장과 카테고리별 메뉴 목록
    fun getMenuListByStoreAndCategory(
        @Path("storeId") storeId: String,
        @Path("categoryId") categoryId: Int
    ): Call<List<Menu>>


    @GET("store/{storeId}")
    fun getStoreInfo(@Path("storeId") storeId: String): Call<Store>
}