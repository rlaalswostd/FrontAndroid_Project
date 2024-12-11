package com.example.neworderapp.viewmodel

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.window.application
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.neworderapp.R
import com.example.neworderapp.TablePreferenceManager
import com.example.neworderapp.data.Category
import com.example.neworderapp.data.Menu
import com.example.neworderapp.client.MenuClient
import com.example.neworderapp.client.OrderClient
import com.example.neworderapp.client.TableCheckerClient
import com.example.neworderapp.data.Store
import com.example.neworderapp.dto.OrderItemRequest
import com.example.neworderapp.dto.OrderRequest
import com.example.neworderapp.dto.TableStatusResponse
import com.example.neworderapp.service.TableChecker
import com.google.gson.Gson

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.client.mqtt.datatypes.MqttQos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    // 둘 중 하나로 통일 (menusByCategory 추천)
    private val menusByCategory = mutableMapOf<Int, MutableLiveData<List<Menu>>>()
    private val appContext = application.applicationContext
    private var mqttClient: Mqtt3Client? = null
    private var mqttConnected = false

    private val _store = MutableLiveData<Store>()
    val store: LiveData<Store> get() = _store

    private val _cartItems = MutableLiveData<Map<Menu, Int>>()
    val cartItems: LiveData<Map<Menu, Int>> get() = _cartItems

    private val cart = mutableMapOf<Menu, Int>()  // 장바구니 데이터 저장용

    // categoryList 프로퍼티 (LiveData)
    private val _categoryList = MutableLiveData<List<Category>>()
    val categoryList: LiveData<List<Category>> get() = _categoryList

    // menuList 프로퍼티 (LiveData)
    private val _menuList = MutableLiveData<List<Menu>>()
    val menuList: LiveData<List<Menu>> get() = _menuList

    //토스트 꾸미기
    fun showCustomToast(context: Context, message: String, iconRes: Int, highlightText: String) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_menuselect, null)

        val toastIcon = layout.findViewById<ImageView>(R.id.toast_icon)
        val toastMessage = layout.findViewById<TextView>(R.id.toast_message)

        toastIcon.setImageResource(iconRes) // 아이콘 설정

        // 색상 폰트
        val spannableMessage = SpannableString(message)
        val start = message.indexOf(highlightText)
        if (start != -1) {
            val colorSpan = ForegroundColorSpan(context.getColor(R.color.sigu)) // 원하는 색상
            spannableMessage.setSpan(
                colorSpan,
                start,
                start + highlightText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        toastMessage.text = spannableMessage // 스타일이 적용된 메시지 설정

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout // 커스텀 레이아웃 설정
        toast.show()
    }

    fun isMqttConnected(): Boolean {
        return mqttConnected && mqttClient?.state == MqttClientState.CONNECTED
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mqttClient?.toAsync()?.disconnect()
        } catch (e: Exception) {
            Log.e("MQTT", "Error disconnecting", e)
        }
        mqttClient = null
    }

    //mqtt
    fun initializeMqtt(context: Context) {
        try {
            val clientId = "android-client-${System.currentTimeMillis()}"
            mqttClient = Mqtt3Client.builder()
                .identifier(clientId)
                .serverHost("175.126.37.21")
                .serverPort(11883)
                .build()

            mqttClient?.toAsync()?.connectWith()
                ?.simpleAuth()
                ?.username("aaa")
                ?.password("bbb".toByteArray())
                ?.applySimpleAuth()
                // ?.automaticReconnectWithDefaultConfig() // 자동 재연결 설정 추가
                ?.send()
                ?.whenComplete { _, throwable ->
                    if (throwable != null) {
                        Log.e("MQTT", "Error connecting to broker", throwable)
                        mqttConnected = false

                    } else {
                        Log.d("MQTT", "Connected to broker on port 11883")
                        mqttConnected = true

                    }
                }
        } catch (e: Exception) {
            Log.e("MQTT", "MQTT 초기화 오류", e)
            mqttConnected = false
        }
    }


    //t
    private fun subscribeToMenuUpdates(storeId: String, context: Context) {
        if (!isMqttConnected() || mqttClient == null) {
            Log.e("MQTT", "구독 불가 - MQTT 연결되지 않음")
            return
        }

        val topic = "bsit/class403/$storeId/menu"
        Log.d("MQTT", "구독 시도: $topic")

        try {
            mqttClient?.toAsync()
                ?.subscribeWith()
                ?.topicFilter(topic)
                ?.qos(MqttQos.AT_LEAST_ONCE)
                ?.callback { publish ->
                    val message = String(publish.payloadAsBytes)
                    Log.d("MQTT", "메시지 수신: $message")

                    try {
                        val updateData = Gson().fromJson(message, Map::class.java)
                        Log.d("MQTT", "파싱된 데이터: $updateData")

                        if (updateData["action"] == "menuUpdate") {
                            val menuId = (updateData["menuId"] as Double).toLong()
                            val isAvailable = (updateData["isAvailable"] as Double).toInt()
                            Log.d("MQTT", "메뉴 업데이트 - ID: $menuId, 상태: $isAvailable")

                            // UI 쓰레드에서 메뉴 상태 업데이트
                            Handler(Looper.getMainLooper()).post {
                                updateMenuAvailability(menuId, isAvailable)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MQTT", "메시지 처리 중 오류: ${e.message}")
                        e.printStackTrace()
                    }
                }
                ?.send()
                ?.whenComplete { _, throwable ->
                    if (throwable != null) {
                        Log.e("MQTT", "구독 실패", throwable)
                    } else {
                        Log.d("MQTT", "구독 성공: $topic")
                    }
                }
        } catch (e: Exception) {
            Log.e("MQTT", "구독 설정 오류", e)
        }
    }


    // MQTT 메시지 처리 부분 수정
    private fun updateMenuAvailability(menuId: Long, isAvailable: Int) {
        // 각 카테고리별 메뉴 업데이트
        menusByCategory.forEach { (categoryId, liveData) ->
            liveData.value?.let { menuList ->
                val updatedList = menuList.map { menu ->
                    if (menu.id == menuId) {
                        menu.copy(isAvailable = isAvailable)
                    } else {
                        menu
                    }
                }
                liveData.postValue(updatedList)
            }
            Log.d(
                "MenuViewModel",
                "menusByCategory: ${menusByCategory.mapValues { it.value.value }}"
            )

        }
        // 전체 메뉴 리스트도 업데이트
        _menuList.value?.let { currentList ->
            val updatedList = currentList.map { menu ->
                if (menu.id == menuId) {
                    menu.copy(isAvailable = isAvailable)
                } else {
                    menu
                }
            }
            _menuList.postValue(updatedList)
            Log.d("MenuViewModel", "_menuList: ${_menuList.value}")
        }

    }

    // 메시지 발행 함수
    private fun publishOrderMessage(orderRequest: OrderRequest) {
        if (!mqttConnected || mqttClient == null) {
            Log.e("MQTT", "MQTT client not connected")
            return
        }

        try {
            val topic = "bsit/class403/store/${orderRequest.storeId}"
            val cartItems = _cartItems.value ?: return

            val mqttOrderData = mapOf(
                "action" to "create",
                "storeId" to orderRequest.storeId,  // storeId 추가
                "tableNumber" to orderRequest.tableNumber,
                "items" to cartItems.map { (menu, quantity) ->
                    mapOf(
                        "menuId" to menu.id,
                        "menuName" to menu.name,
                        "quantity" to quantity
                    )
                },
                "orderTime" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())
            )

            val message = Gson().toJson(mqttOrderData)

            mqttClient?.toAsync()
                ?.publishWith()
                ?.topic(topic)
                ?.payload(message.toByteArray())
                ?.qos(MqttQos.AT_LEAST_ONCE)
                ?.send()
                ?.whenComplete { _, throwable ->
                    if (throwable != null) {
                        Log.e("MQTT", "Error publishing message", throwable)
                    } else {
                        Log.d("MQTT", "Message published successfully")
                    }
                }
        } catch (e: Exception) {
            Log.e("MQTT", "Error publishing message", e)
        }
    }


    // 카테고리 목록 가져오기
    fun fetchCategoryList(storeId: String): LiveData<List<Category>> {
        MenuClient.menuApiService.getCategoryList().enqueue(object : Callback<List<Category>> {
            override fun onResponse(
                call: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                if (response.isSuccessful) {
                    val filteredCategories = response.body()?.filter { category ->
                        category.storeId == storeId // 카테고리의 storeId가 매장 ID와 일치하는지 확인
                    }?.sortedBy { it.displayOrder }    // displayOrder를 기준으로 오름차순 정렬
                    _categoryList.value = filteredCategories
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Log.e("MenuViewModel", "Failed to fetch categories", t)
                // 실패 로그찍기
            }
        })
        return categoryList
    }


    // 메뉴 목록 가져오기 함수 수정
    fun getMenusByStoreAndCategory(storeId: String, categoryId: Int): LiveData<List<Menu>> {
        // 해당 카테고리의 LiveData가 없으면 새로 생성
        if (!menusByCategory.containsKey(categoryId)) {
            menusByCategory[categoryId] = MutableLiveData()
        }

        // API 호출하여 초기 데이터 로드
        MenuClient.menuApiService.getMenuListByStoreAndCategory(storeId, categoryId)
            .enqueue(object : Callback<List<Menu>> {
                override fun onResponse(call: Call<List<Menu>>, response: Response<List<Menu>>) {
                    try {
                        if (response.isSuccessful && response.body() != null) {
                            val menuList = response.body()
                            Log.d("MenuList",menuList.toString())

                            // categoryId에 해당하는 LiveData 업데이트
                            menusByCategory[categoryId]?.value = menuList
                            updateFullMenuList()

                            // 성공적으로 불러온 메뉴 리스트 로그
                            Log.d("MenuViewModel", "성공적으로 로드된 메뉴 (카테고리 $categoryId): $menuList")
                            Log.d(
                                "MenuViewModel",
                                "메뉴 로딩 성공 - 카테고리 $categoryId, 개수: ${menuList?.size}"
                            )
                            Log.d(
                                "MenuViewModel",
                                "menusByCategory[categoryId]: ${menusByCategory[categoryId]?.value}"
                            )
                            Log.d("MenuViewModel", "_menuList: ${_menuList.value}")
                            menuList?.forEach { menu ->
                                Log.d(
                                    "MenuViewModel",
                                    "메뉴 정보: ${menu.name}, ID: ${menu.id}, 가격: ${menu.price}, 상태: ${menu.isAvailable}"
                                )
                            }
                            // 전체 메뉴 리스트 업데이트
                            updateFullMenuList()
                        } else {
                            Log.e("MenuViewModel", "메뉴 조회 실패 - 응답 코드: ${response.code()}")
                            Log.e("MenuViewModel", "에러 메시지: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("MenuViewModel", "JSON 파싱 오류", e)
                        Log.e("MenuViewModel", "Raw 응답: ${response.raw()}")
                    }
                }

                override fun onFailure(call: Call<List<Menu>>, t: Throwable) {
                    Log.e("MenuViewModel", "네트워크 요청 실패", t)

                    // 코루틴 스코프에서 실행
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // 새로운 콜 객체 생성하여 동기 요청
                            val newCall = call.clone()
                            val response = newCall.execute()

                            // 메인 스레드에서 UI 업데이트
                            withContext(Dispatchers.Main) {
                                response.errorBody()?.let { errorBody ->
                                    val errorString = errorBody.string()
                                    if (errorString.isNotEmpty()) {
                                        Log.e("MenuViewModel", "Error Body: $errorString")
                                    }
                                }

                                Log.e("MenuViewModel", "Response Code: ${response.code()}")
                                Log.e("MenuViewModel", "Response Message: ${response.message()}")

                                // 실패시 빈 리스트로 업데이트
                                menusByCategory[categoryId]?.value = emptyList()
                            }
                        } catch (e: Exception) {
                            Log.e("MenuViewModel", "Raw 응답 로깅 실패", e)
                            withContext(Dispatchers.Main) {
                                menusByCategory[categoryId]?.value = emptyList()
                            }
                        }
                    }
                }
            })

        return menusByCategory[categoryId]!!
    }


    // 전체 메뉴 리스트 업데이트 함수 추가
    private fun updateFullMenuList() {
        val allMenus = menusByCategory.values.flatMap { it.value ?: emptyList() }
        _menuList.value = allMenus

        Log.d("MenuViewModel", "updateFullMenuList() 호출됨")
        Log.d("MenuViewModel", "menusByCategory: ${menusByCategory.mapValues { it.value.value }}")
    }

    // 장바구니 관련 메서드들
    fun addToCart(menu: Menu, context: Context) {
        if (cart.containsKey(menu)) {
            val message1 = "${menu.name}은(는) 이미 장바구니에 있습니다."
            showCustomToast(context, message1, R.drawable.toasticon1, menu.name)
            return // 이미 장바구니에 있으면 아무 일도 일어나지 않음
        }

        // 새로운 메뉴는 수량을 1로 설정하여 추가
        cart[menu] = 1

        // LiveData 갱신
        _cartItems.value = HashMap(cart)
        Log.d("MenuViewModel", "Added to cart: ${menu.name}")
        Log.d("MenuViewModel", "Added to cart: ${menu.price}")
        Log.d("MenuViewModel", "Added to cart: ${menu.id}")
        Log.d("MenuViewModel", "Added to cart: ${menu.categoryId}")
        val message2 = "${menu.name} 장바구니에 담겼습니다"
        showCustomToast(context, message2, R.drawable.toasticon1, menu.name)

    }

    // 카트에서 메뉴 삭제
    fun removeCartItem(menu: Menu) {
        cart.remove(menu)
        _cartItems.value = cart// LiveData 갱신
    }

    fun increaseCartItemQuantity(menu: Menu, context: Context) {
        if (cart.containsKey(menu)) {
            cart[menu] = (cart[menu] ?: 0) + 1
        } else {

            cart[menu] = 1
        }
        // LiveData 갱신
        _cartItems.value = cart

    }


    fun decreaseFromCart(menu: Menu, context: Context) {
        if (cart.containsKey(menu)) {
            val currentQuantity = cart[menu] ?: 1
            if (currentQuantity > 1) {
                cart[menu] = currentQuantity - 1
                _cartItems.value = cart
            }

        }
    }

    // 주문 관련 메서드들
    fun showOrderCompleteDialog(context: Context) {

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_order_com, null) // 커스텀 레이아웃

        // Dialog 객체 생성
        val dialog = Dialog(context)
        dialog.setContentView(view)

        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        val layoutParams = window?.attributes

        val display = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        display.defaultDisplay.getRealSize(size)
        layoutParams?.width = size.x
        layoutParams?.height = size.y
        window?.attributes = layoutParams

        val okButton = view.findViewById<Button>(R.id.dialog_ok_button)
        okButton.setOnClickListener {
            dialog.dismiss()  // "확인" 버튼을 누르면 다이얼로그 닫기
        }

        // 다이얼로그를 띄웁니다.
        dialog.show()

        Handler().postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss() // 자동으로 다이얼로그 닫기
            }
        }, 5000)  // 5000ms (5초) 후 실행
    }

    fun ordering(context: Context, drawerLayout: DrawerLayout) {
        val storeId = _store.value?.storeId ?: return

        Log.d("Ordering", "Store ID: $storeId")

        val tableNumber = TablePreferenceManager(context).getTableNumber()

        // 테이블 상태 확인
        checkTableOccu(context, tableNumber) { isOccupied ->
            if (!isOccupied) {  // isOccupied db값이 1 , true 이면 주문가능함
                // 테이블이 사용 가능하면 주문 진행
                // 카트의 메뉴 아이템과 수량을 OrderItemRequest로 변환
                val orderItems = _cartItems.value?.map { (menu, quantity) ->
                    OrderItemRequest(
                        menuId = menu.id,
                        quantity = quantity,
                        request = ""  // 사용자가 요청 사항을 추가하면 여기에 담으면 됨
                    )
                } ?: emptyList()

                // OrderRequest 생성
                val orderRequest = OrderRequest(
                    storeId = storeId,
                    tableNumber = tableNumber,
                    items = orderItems
                )

                // Retrofit을 사용하여 백엔드 API 호출
                OrderClient.orderApiService.createOrder(orderRequest).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            publishOrderMessage(orderRequest)
                            cart.clear()
                            _cartItems.value = cart
                            showOrderCompleteDialog(context)
                            drawerLayout.closeDrawers()
                        } else {
                            // 주문 실패
                            Log.e("OrderFailure", "Error Code: ${response.code()}")
                            Log.e("OrderFailure", "Error Message: ${response.message()},${tableNumber}")
                            Toast.makeText(context, "주문 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        // 네트워크 오류 또는 기타 실패 처리
                        Log.e("MenuViewModel", "주문 실패", t)
                        Toast.makeText(context, "주문 실패. 네트워크를 확인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // 테이블이 사용 중이거나 존재하지 않으면 주문 불가
                Toast.makeText(context, "해당 테이블은 사용 불가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Store 정보를 DB에서 불러오는 함수
    fun fetchStoreInfo(storeId: String) {
        MenuClient.menuApiService.getStoreInfo(storeId).enqueue(object : Callback<Store> {
            override fun onResponse(call: Call<Store>, response: Response<Store>) {
                if (response.isSuccessful) {
                    response.body()?.let { store ->
                        _store.value = store
                        Log.d("MenuViewModel", "스토어 정보 로드 완료: ${store.storeName}")

                        // MQTT가 연결된 상태일 때만 구독 시도
                        if (mqttConnected && mqttClient != null) {
                            subscribeToMenuUpdates(store.storeId, appContext)
                        }
                    }
                } else {
                    Log.e("MenuViewModel", "스토어 정보 로드 실패: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Store>, t: Throwable) {
                Log.e("MenuViewModel", "스토어 정보 로드 중 오류 발생", t)
            }
        })
    }



    fun checkTableOccu(context: Context, tableNumber: String, callback: (Boolean) -> Unit) {
        val storeId = _store.value?.storeId ?: return
        val tableChecker =
            TableCheckerClient.tableCheckerInstance.create(TableChecker::class.java)

        // 테이블 상태 확인 API 호출
        tableChecker.checkTableExistsandoccu(storeId, tableNumber)
            .enqueue(object : Callback<TableStatusResponse> {
                override fun onResponse(
                    call: Call<TableStatusResponse>,
                    response: Response<TableStatusResponse>
                ) {
                    if (response.isSuccessful) {
                        val tableStatus = response.body()
                        if (tableStatus != null && tableStatus.tableExists && !tableStatus.occupied) {
                            // 테이블이 존재하고 사용 중이지 않으면 주문 가능
                            callback(true)
                        } else {
                            // 테이블이 사용 중이거나 존재하지 않으면 주문 불가
                            callback(false)
                        }
                    } else {
                        // 서버 오류
                        Toast.makeText(context, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }

                override fun onFailure(call: Call<TableStatusResponse>, t: Throwable) {
                    // 네트워크 오류
                    Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            })
    }

}
