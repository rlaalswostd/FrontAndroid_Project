package com.example.neworderapp

import android.content.Context
import android.content.SharedPreferences


class TablePreferenceManager(context:Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("TablePreferences", Context.MODE_PRIVATE)

    companion object {
        private const val TABLE_NUMBER_KEY = "table_number"
        private const val STORE_ID_KEY = "store_id"  // storeId 키 추가
    }

    // tableNumber 값을 저장
    fun saveTableNumber(tableNumber: String) {
        prefs.edit().putString(TABLE_NUMBER_KEY, tableNumber).apply()
    }

    // tableNumber 값을 가져오기
    fun getTableNumber(): String {
        return prefs.getString(TABLE_NUMBER_KEY,"1")?: "1" // 기본값
    }

    // tableNumber 값을 삭제
    fun clearTableNumber() {
        prefs.edit().remove(TABLE_NUMBER_KEY).apply()
    }

    // storeId를 가져오는 메서드 (예시 추가)
    fun getStoreId(): String? {
        return prefs.getString(STORE_ID_KEY, null)  // storeId가 없으면 null 반환

    }
    // storeId 저장
    fun saveStoreId(storeId: String) {
        prefs.edit().putString(STORE_ID_KEY, storeId).apply()
    }

}