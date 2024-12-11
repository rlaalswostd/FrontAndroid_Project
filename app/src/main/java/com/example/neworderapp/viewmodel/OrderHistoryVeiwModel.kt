package com.example.neworderapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neworderapp.client.OrderHistoryClient
import com.example.neworderapp.dto.OrderHistory
import com.example.neworderapp.dto.OrderedMenuResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
//주문내역 라이브데이터 감시자
class OrderHistoryViewModel(private val storeId: String, private val tableNumber: String) : ViewModel() {
    private val _orderHistory = MutableLiveData<OrderHistory?>()
    val orderHistory: LiveData<OrderHistory?> = _orderHistory

    private val _orderedMenus = MutableLiveData<List<OrderedMenuResponse>>()
    val orderedMenus: LiveData<List<OrderedMenuResponse>> = _orderedMenus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // 주문 상태를 체크하는 메서드 추가
    fun checkOrderStatus(): Boolean {
        return !(_orderedMenus.value.isNullOrEmpty() && _orderHistory.value == null)
    }

    init {
        fetchOrderHistory()
        fetchOrderedMenus()
    }

    fun fetchOrderHistory() {
        _isLoading.value = true
        val apiService = OrderHistoryClient.orderHistoryService
        apiService.getOrderHistory(storeId, tableNumber).enqueue(object :
            Callback<List<OrderHistory>> {
            override fun onResponse(call: Call<List<OrderHistory>>, response: Response<List<OrderHistory>>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    _orderHistory.value = response.body()!![0]
                } else {
                    _orderHistory.value = null // 명시적으로 null 설정
                }
            }

            override fun onFailure(call: Call<List<OrderHistory>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "주문 내역을 불러오지 못했습니다."
                _orderHistory.value = null
            }
        })
    }

    fun fetchOrderedMenus() {
        _isLoading.value = true
        val apiService = OrderHistoryClient.orderHistoryService
        apiService.getOrderedMenus(storeId, tableNumber).enqueue(object :
            Callback<List<OrderedMenuResponse>> {
            override fun onResponse(call: Call<List<OrderedMenuResponse>>, response: Response<List<OrderedMenuResponse>>) {
                _isLoading.value = false
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    _orderedMenus.value = response.body()!!
                } else {
                    _orderedMenus.value = emptyList() // 빈 리스트로 설정
                }
            }

            override fun onFailure(call: Call<List<OrderedMenuResponse>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "주문 메뉴를 불러오지 못했습니다."
                _orderedMenus.value = emptyList()
            }
        })
    }

    fun clearOrderHistory() {
        // 주문 내역과 관련된 LiveData를 초기화
        _orderHistory.value = null
        _orderedMenus.value = emptyList()
    }


    class OrderHistoryViewModelFactory(
        private val storeId: String,
        private val tableNumber: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OrderHistoryViewModel(storeId, tableNumber) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}