package com.example.neworderapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.neworderapp.adapter.CartAdapter
import com.example.neworderapp.adapter.CategoryAdapter
import com.example.neworderapp.adapter.CategoryEleAdapter
import com.example.neworderapp.adapter.OrderHistoryAdapter
import com.example.neworderapp.client.CategoryEleClient
import com.example.neworderapp.client.OrderStatusClient
import com.example.neworderapp.data.Category
import com.example.neworderapp.data.OrderHistoryItem
import com.example.neworderapp.databinding.ActivityMainBinding
import com.example.neworderapp.viewmodel.MenuViewModel
import com.example.neworderapp.viewmodel.OrderHistoryViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding
    private lateinit var cartAdapter: CartAdapter
    private lateinit var tablePreferenceManager: TablePreferenceManager
    private lateinit var categoryAdapter: CategoryEleAdapter
    private lateinit var orderDetail: TextView
    private lateinit var totalPriceTextView: TextView
    private lateinit var OrderTimeTextView: TextView
    private lateinit var totalAmountTextView: TextView
    private lateinit var orderHistoryViewModel: OrderHistoryViewModel
    private lateinit var orderHistoryRecyclerView: RecyclerView
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter

    private val menuViewModel: MenuViewModel by viewModels()


    private var newStoreId:String?=null
    private var pressDuration = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val orderhistoryItemList = mutableListOf<OrderHistoryItem>()

    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable {
        // 3분 후 OrderFirstActivity로 이동
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // 다크모드 삭제 무조건 라이트모드로
        setContentView(R.layout.activity_main)

        // 상태바와 네비게이션 바 숨기기
        val window: Window = window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 네비게이션 바도 숨기려면 다음 코드도 추가
        val decorView: View = window.getDecorView()
        val uiOptions =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions


        // 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // 타이머 시작
//        startTimeoutCountdown()

        //품목 개수 표시
        orderDetail = findViewById(R.id.orderDetail)
        // val menuViewModel: MenuViewModel = ViewModelProvider(this).get(MenuViewModel::class.java)

        orderHistoryRecyclerView = findViewById(R.id.orderHistoryRecyclerView)
        // DrawerLayout 초기화
        drawerLayout = binding.drawerLayout
        tablePreferenceManager = TablePreferenceManager(this)


        // RecyclerView 어댑터 설정 (주문 내역)
        orderHistoryAdapter = OrderHistoryAdapter(emptyList())
        binding.orderHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = orderHistoryAdapter

            // 메뉴 목록을 관찰하고 메뉴의 사용 가능 상태를 출력
            menuViewModel.menuList.observe(this@MainActivity, Observer { menuList ->
                Log.d("MenuStatus", "menuList updated: ${menuList.size} items loaded")

                // menuList가 null이 아니고, 데이터가 존재하는지 확인
                menuList?.forEach { menu ->
                    Log.d("MenuStatus", "${menu.name}의 상태는 ${if (menu.isAvailable == 1) "사용 가능" else "사용 불가능"}입니다.")

                    if (menu.isAvailable == 1) {
                        Log.d("MenuStatus", "${menu.name}은 사용 가능합니다.")
                    } else {
                        Log.d("MenuStatus", "${menu.name}은 사용 불가능합니다.")
                    }
                }
            })

        }


        //스토어 아이디 지정
         newStoreId = "3" //


        // preference store id 값사용
        tablePreferenceManager.saveStoreId(newStoreId!!)

        Log.d("MainActivity", "storeId: $newStoreId")


        //배포시마다 스토어아이디 지정해주기
        menuViewModel.fetchStoreInfo(newStoreId!!)

        //table번호 가져오기
        val tableNumber = tablePreferenceManager.getTableNumber()


        val tableNumberTextView: TextView = findViewById(R.id.tableNumber)
        if (tableNumber.isNotEmpty()) {
            tableNumberTextView.text = "T$tableNumber"
            Log.d("TableInfo", "현재 Table Number: $tableNumber")

        } else {
            tableNumberTextView.text = "Table Number 를 설정해주세요"
        }

        binding.orderButton.isEnabled = false  // 초기 상태: 비활성화
        menuViewModel.cartItems.observe(this, Observer { cartItems ->
            Log.d("MainActivity", "Cart items updated: $cartItems")  // 로그 추가

            val itemCount = cartItems.size
            updateOrderDetailText(itemCount)

            binding.orderButton.isEnabled = cartItems.isNotEmpty()
        })

        totalPriceTextView = findViewById(R.id.totalPriceTextView)


        // 테이블 번호 설정 모션 -> 5초 꾹누르기
        tableNumberTextView.setOnTouchListener { v, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    pressDuration = System.currentTimeMillis()  // 누르기 시작한 시간 기록
                    true
                }

                android.view.MotionEvent.ACTION_UP -> {
                    if (System.currentTimeMillis() - pressDuration >= 5000) {
                        // 5초 이상 눌렀다면
                        navigateToSetTableActivity()
                    }
                    true
                }

                else -> false
            }
        }
        // 카테고리 엘리베이터 불러오기
        categoryAdapter = CategoryEleAdapter(emptyList()) { category, position ->
            smoothScrollToCategoryPosition(position)
        }
        binding.categoryRecyclerView.adapter = categoryAdapter
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        loadCategoriesFromApi(newStoreId!!)
        menuViewModel.initializeMqtt(applicationContext)
        menuViewModel.store.observe(this, Observer { store ->
            if(store != null){
                println("Store 정보가 아직 로드되지 않았습니다.")
            }

            Log.d("MainActivity", "storeName: ${store.storeName}")
            val storeNameTextView: TextView = findViewById(R.id.storeNameTextView)

            // storeName이 null이면 기본값, 아니면 줄 바꿈 처리
            val originalText = store.storeName ?: "스토어 정보 없음"

            // 1. TextView의 padding과 width 고려
            val availableWidthPx =
                storeNameTextView.width - storeNameTextView.paddingStart - storeNameTextView.paddingEnd

            // 2. TextPaint로 텍스트 너비 계산
            val textPaint = storeNameTextView.paint
            val formattedText = buildString {
                var currentLine = ""
                for (word in originalText.split(" ")) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    if (textPaint.measureText(testLine) <= availableWidthPx) {
                        currentLine = testLine
                    } else {
                        appendLine(currentLine.trim())
                        currentLine = word
                    }
                }
                if (currentLine.isNotEmpty()) append(currentLine.trim())
            }

            storeNameTextView.text = formattedText ?: "스토어 정보 없음"
        })

        // RecyclerView 레이아웃 설정  메뉴판
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // CategoryAdapter 인스턴스를 유지
        // 카테고리 목록을 관찰하여 업데이트

        menuViewModel.fetchCategoryList(newStoreId!!).observe(this, Observer { categories ->
            // CategoryAdapter를 설정하여 카테고리와 메뉴를 함께 표시
            binding.recyclerView.adapter =
                CategoryAdapter(categories, menuViewModel, this, newStoreId!!)
        })

        // 장바구니 RecyclerView 설정
        cartAdapter = CartAdapter(menuViewModel, { itemCount ->
            updateOrderDetailText(itemCount) // 수량 업데이트
        }, { totalPrice ->
            updateTotalPrice(totalPrice) // 총 가격 업데이트
        })
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cartRecyclerView.adapter = cartAdapter

        // 장바구니 관찰
        menuViewModel.cartItems.observe(this, Observer { cartItems ->
            cartAdapter.submitList(cartItems.keys.toList()) {
              cartAdapter.notifyDataSetChanged()
            }

        })

        //주문완료 버튼
        binding.orderButton.setOnClickListener {
            // LayoutInflater를 사용하여 custom_dialog.xml을 View로 변환
            val inflater = LayoutInflater.from(this)
            val customView = inflater.inflate(R.layout.customalertorder, null)

            // AlertDialog.Builder 생성 및 커스텀 뷰 설정
            val builder = AlertDialog.Builder(this)
            builder.setView(customView) // 커스텀 레이아웃 적용

            // 다이얼로그 UI 요소 가져오기
            val confirmButton = customView.findViewById<Button>(R.id.customalertorderButton1)
            val cancelButton = customView.findViewById<Button>(R.id.customalertorderButton2)

            // 다이얼로그 생성
            val dialog = builder.create()

            // 확인 버튼 클릭 이벤트
            confirmButton.setOnClickListener {
                // 주문 처리 로직 실행
                menuViewModel.ordering(this, binding.drawerLayout)

                // 주문 후 장바구니 비우기 및 주문 내역 갱신
                menuViewModel.cartItems.observe(this, Observer { cartItems ->
                    orderHistoryViewModel.fetchOrderHistory()
                    orderHistoryViewModel.fetchOrderedMenus()
                    updateTotalPrice(0.0) // 총 금액 초기화
                })

                dialog.dismiss() // 다이얼로그 닫기
            }

            // 취소 버튼 클릭 이벤트
            cancelButton.setOnClickListener {
                dialog.dismiss() // 다이얼로그 닫기
            }

            // 다이얼로그 표시
            dialog.show()
        }

        // 장바구니 열기 버튼 클릭 시 DrawerLayout을 열기
        binding.cartButton.setOnClickListener {
            drawerLayout.open()
        }

        // 장바구니 닫기 버튼 클릭 시 DrawerLayout을 닫기
        binding.closeCartButton.setOnClickListener {
            drawerLayout.close()
        }
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)


        // ==== 카테고리
        // RecyclerView 레이아웃 설정
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 카테고리 목록을 관찰하여 업데이트
        menuViewModel.fetchCategoryList(newStoreId!!).observe(this, Observer { categories ->
            binding.recyclerView.adapter =
                CategoryAdapter(categories, menuViewModel, this, newStoreId!!)
            binding.recyclerView.post {
                // 데이터가 로드된 후 첫 번째 항목으로 부드럽게 스크롤
                (binding.recyclerView.layoutManager as LinearLayoutManager)
                    .smoothScrollToPosition(binding.recyclerView, null, 0)
            }
        })


        // 장바구니 닫기 버튼 클릭 시 DrawerLayout을 닫기
        binding.closeOrderHistoryButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        //주문내역 버튼
        binding.billBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)

        }

        //주문내역에 표시될 data
        OrderTimeTextView = findViewById(R.id.orderTimeTextView)
        totalAmountTextView = findViewById(R.id.totalAmountTextView)

        var storeId = newStoreId

        orderHistoryViewModel = ViewModelProvider(
            this,
            OrderHistoryViewModel.OrderHistoryViewModelFactory(newStoreId!!, tableNumber)
        ).get(OrderHistoryViewModel::class.java)

// 주문 내역 LiveData 관찰
        orderHistoryViewModel.orderHistory.observe(this) { orderHistory ->
            // 주문 내역이 있을 때만 UI 업데이트
            orderHistory?.let {
                val OrderTimeTextView: TextView = findViewById(R.id.orderTimeTextView)
                val totalAmountTextView: TextView = findViewById(R.id.totalAmountTextView)

                OrderTimeTextView.text = "마지막 주문 시간: ${it.lastOrderTime ?: "정보 없음"}"

                val formattedTotalSum = try {
                    NumberFormat.getNumberInstance(Locale.KOREA).format(it.totalSum.toLong())
                } catch (e: Exception) {
                    Log.e("OrderHistory", "Total sum formatting error: ${e.message}")
                    "0"
                }
                totalAmountTextView.text = "\u20A9 ${formattedTotalSum}"
            }

            // 버튼 상태 업데이트
            binding.payCallBtn.isEnabled = orderHistoryViewModel.checkOrderStatus()
        }


        // 에러 메시지 처리
        orderHistoryViewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

        //주문메뉴 불러오는 리사이클러뷰 어댑터
        orderHistoryViewModel.orderedMenus.observe(this) { orderedMenus ->
            // 주문된 메뉴 리스트를 어댑터에 전달
            orderHistoryAdapter = OrderHistoryAdapter(orderedMenus)
            orderHistoryRecyclerView.adapter = orderHistoryAdapter
            Log.d("페치전", "Order History Data: $orderedMenus")
            binding.payCallBtn.isEnabled = orderHistoryViewModel.checkOrderStatus()
        }

        // 계산할게요 클릭 시
        binding.payCallBtn.setOnClickListener {
            // 커스텀 레이아웃 인플레이트
            val dialogView = layoutInflater.inflate(R.layout.order_last, null)
            val customDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            val playerView = dialogView.findViewById<PlayerView>(R.id.playerView)

            val player = ExoPlayer.Builder(this).build()
            playerView.player = player

            // 재생할 미디어 아이템 설정 (예시로 MP4 파일 URL 사용)
            val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.karinabeer}")
            val mediaItem = MediaItem.fromUri(videoUri)
            player.setMediaItem(mediaItem)
            player.repeatMode = Player.REPEAT_MODE_ALL
            player.playWhenReady = true
            playerView.useController = false
            player.prepare()
            player.play()


            // 다이얼로그 요소 가져오기
            val confirmButton = dialogView.findViewById<AppCompatButton>(R.id.orderlastOK)
            val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.orderlastNO)

            // 확인 버튼 클릭 이벤트
            confirmButton.setOnClickListener {
                val storeId = newStoreId
                val call = OrderStatusClient.orderStatusService.updateOrderStatusToCompleted(
                    storeId, tableNumber
                )

                call?.enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(
                        call: Call<Map<String, Any>>,
                        response: Response<Map<String, Any>>,
                    ) {
                        if (response.isSuccessful) {
                            orderHistoryViewModel.clearOrderHistory()
                            OrderTimeTextView.text = "마지막 주문 시간: 없음 "
                            binding.totalAmountTextView.text = "\u20A9 0"
                            orderHistoryViewModel.fetchOrderHistory()
                            binding.payCallBtn.isEnabled = false
                            player.release()
                            customDialog.dismiss() // 다이얼로그 닫기
                            //주문 후 창닫기
                            binding.drawerLayout.closeDrawer(GravityCompat.END)

                            //계산 할게요 확인 클릭 후 나올 영상
                            val intent = Intent(applicationContext, OrderBehindFinalActivity::class.java)
                            startActivity(intent)

                            finish()

                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "계산 중 오류가 발생했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                            .show()
                        customDialog.dismiss() // 다이얼로그 닫기
                    }
                })
            }

            // 취소 버튼 클릭 이벤트
            cancelButton.setOnClickListener {
                player.clearVideoSurface()
                player.release()
                customDialog.dismiss() // 다이얼로그 닫기
            }

            customDialog.show() // 다이얼로그 표시
        }












        //oncreate
    }


    private fun updateOrderDetailText(itemCount: Int) {
        orderDetail.text = "$itemCount 품목"  //품목표시 텍스트
    }

    private fun updateTotalPrice(totalPrice: Double) {
        Log.d("MainActivity", "Updating total price: $totalPrice")
        val formattedTotalPrice = NumberFormat.getNumberInstance().format(totalPrice)
        totalPriceTextView.text = "$formattedTotalPrice 원"
    }

    // 테이블 번호 설정 화면으로 이동하는 메서드
    private fun navigateToSetTableActivity() {
        val storeId = newStoreId
        val intent = Intent(this, SetTableActivity::class.java)
        intent.putExtra("newStoreId",storeId)
        startActivity(intent)
    }


    private fun loadCategoriesFromApi(storeId: String) {
        val service = CategoryEleClient.categoryEleService
        service.getCategoriesByStore(storeId).enqueue(object : Callback<List<Category>> {
            override fun onResponse(
                call: Call<List<Category>>,
                response: Response<List<Category>>,
            ) {
                if (response.isSuccessful) {
                    val categories = response.body()?.sortedBy { it.displayOrder } ?: emptyList() // 받아온 데이터
                    categoryAdapter.updateCategories(categories) // 어댑터에 데이터 전달
                } else {
                    Log.e(
                        "ddd",
                        "API 응답 오류 - 상태 코드: ${response.code()}, 에러 메시지: ${
                            response.errorBody()?.string()
                        }"
                    )
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Log.e("MainActivity", "API 호출 실패: ${t.message}")
            }
        })
    }


    // === 카테고리
    // 부드럽게 카테고리 위치로 스크롤하는 메서드
    private fun smoothScrollToCategoryPosition(position: Int) {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        val smoothScroller = object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START  // 항목을 화면 상단에 맞추도록 설정
            }
        }
        smoothScroller.targetPosition = position
        layoutManager.startSmoothScroll(smoothScroller)
    }

    // 타이머 시작 메서드
    private fun startTimeoutCountdown() {
        timeoutHandler.postDelayed(timeoutRunnable, 600000) // 3분 = 180,000ms
    }

    // 타이머 초기화 메서드
    private fun resetTimeoutCountdown() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        startTimeoutCountdown() // 타이머 재시작
    }

    // 사용자가 화면을 터치할 때 타이머 리셋
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        resetTimeoutCountdown()
        return super.onTouchEvent(event)
    }

    // 액티비티가 종료되면 핸들러 콜백 제거
    override fun onDestroy() {
        super.onDestroy()
        timeoutHandler.removeCallbacks(timeoutRunnable)
    }




}


