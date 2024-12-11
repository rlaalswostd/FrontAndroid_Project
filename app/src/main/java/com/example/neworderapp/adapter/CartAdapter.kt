package com.example.neworderapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.neworderapp.data.Menu
import com.example.neworderapp.databinding.ItemCartBinding
import com.example.neworderapp.viewmodel.MenuViewModel
import java.math.BigDecimal
import java.text.NumberFormat

//메인에서 메뉴 눌렀을때 저장되는 장바구니 입니다
class CartAdapter (private val menuViewModel: MenuViewModel,
                    private val CartItemCountChanger: (Int) -> Unit,
                   var updateTotalPriceCallback: ((Double) -> Unit)? = null
    ):
    ListAdapter<Menu, CartAdapter.CartViewHolder>(CartViewHolder.DiffCallback()) {

    private var menuQuantities = mutableMapOf<Menu, Int>()

    override fun submitList(list: List<Menu>?) {
        super.submitList(list)
        CartItemCountChanger(list?.size ?: 0) // 수량 전달
        updateTotalPrice() // 리스트가 갱신될 때마다 가격 계산
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding,menuQuantities, menuViewModel, :: updateTotalPrice)
    }
 //메뉴 카트에 최초등록
    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val menu = getItem(position)
        // 메뉴를 처음 담을 때 기본 수량을 1로 설정
     // 메뉴 수량을 ViewModel의 cartItems에서 가져오도록 수정
     val quantity = menuViewModel.cartItems.value?.get(menu) ?: 1
     menuQuantities[menu] = quantity  // menuQuantities 명시적 초기화

     holder.bind(menu, quantity)
     updateTotalPrice()  // 총 가격 계산
    }


    private fun updateTotalPrice() {
        var totalPrice = BigDecimal.ZERO
        currentList.forEach { menu ->
            val quantity = menuQuantities[menu] ?: 0
            totalPrice = totalPrice.add(BigDecimal(menu.price).multiply(BigDecimal(quantity))) // BigDecimal 계산
        }
        updateTotalPriceCallback?.invoke(totalPrice.toDouble())
    }


    class CartViewHolder(private val binding: ItemCartBinding,
                         private val menuQuantities: MutableMap<Menu, Int>,
                         private val menuViewModel: MenuViewModel,
                         private val updateTotalPrice: () -> Unit
        ) :
        RecyclerView.ViewHolder(binding.root) {




        //장바구니에 표시할 항목
        fun bind(menu: Menu, quantity: Int) {
            binding.menuName.text = menu.name

            // 50,000원(콤마추가)
            val formattedPrice = NumberFormat.getNumberInstance().format(menu.price)
            binding.menuPrice.text = "$formattedPrice" +"원"
            binding.quantity.text = quantity.toString()

            binding.decreaseButton.isEnabled = quantity > 1
            //버튼 클릭시 행동 작성
            // 수량 증가 버튼 클릭 시
            binding.increaseButton.setOnClickListener {
                // 수량을 1 증가
                menuQuantities[menu] = (menuQuantities[menu] ?: 0) + 1
                binding.quantity.text = menuQuantities[menu].toString() // 수량 갱신
                menuViewModel.increaseCartItemQuantity(menu, binding.root.context)
                updateTotalPrice() // 총 가격 재계산

            }

            // 수량 감소 버튼 클릭 시
            binding.decreaseButton.setOnClickListener {
                val currentQuantity = menuQuantities[menu] ?: 1
                // 수량이 1보다 큰 경우만 감소

                if (currentQuantity > 1) {

                    menuQuantities[menu] = currentQuantity - 1
                    binding.quantity.text = menuQuantities[menu].toString() // 수량 갱신
                    menuViewModel.decreaseFromCart(menu, binding.root.context)
                    updateTotalPrice() // 총 가격 재계산
                }
            }

            //휴지통 버튼 클릭
            binding.deleteMenu.setOnClickListener{
                menuQuantities.remove(menu)
                menuViewModel.removeCartItem(menu)

                updateTotalPrice() // 총 가격 재계산
            }
        }




        class DiffCallback : DiffUtil.ItemCallback<Menu>() {
            override fun areItemsTheSame(oldItem: Menu, newItem: Menu): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Menu, newItem: Menu): Boolean =
                oldItem == newItem
        }

    }



}