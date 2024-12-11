package com.example.neworderapp.service

import com.example.neworderapp.dto.TableStatusResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TableChecker {

    @GET("/api/tables/exists")
    fun checkTableExists(
        @Query("storeId") storeId: String,
        @Query("tableNumber") tableNumber: String
    ):  Call<Boolean>

    @GET("/api/tables/availability")
    fun checkTableExistsandoccu(
        @Query("storeId") storeId: String,
        @Query("tableNumber") tableNumber: String
    ):  Call<TableStatusResponse>

}

