package com.example.neworderapp.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.neworderapp.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.neworderapp.R
import com.example.neworderapp.data.Category
import com.example.neworderapp.data.Menu
import com.example.neworderapp.data.Menuimage
import com.example.neworderapp.databinding.ItemCategoryBinding
import com.example.neworderapp.databinding.ItemMenuBinding
import com.example.neworderapp.viewmodel.MenuViewModel
import java.text.NumberFormat

// 메인 메뉴화면 어댑터입니다

class MenuAdapter(
    private var menuList: List<Menu>,
    private val menuViewModel: MenuViewModel
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private val menuQuantities = mutableMapOf<Menu, Int>()

    init {
        menuList.forEach { menu ->
            menuQuantities[menu] = 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // menuViewModel을 ViewHolder에 전달
        return MenuViewHolder(binding, menuViewModel)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]
        holder.bind(menu)
    }

    override fun getItemCount(): Int = menuList.size

    fun updateMenus(newMenuList: List<Menu>) {
        menuList = newMenuList
        newMenuList.forEach { menu ->
            if (!menuQuantities.containsKey(menu)) {
                menuQuantities[menu] = 0
            }
        }
        notifyDataSetChanged()
    }

    // MenuViewModel을 생성자 매개변수로 추가
    class MenuViewHolder(
        private val binding: ItemMenuBinding,
        private val menuViewModel: MenuViewModel
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(menu: Menu) {
            binding.menuName.text = menu.name
            val formattedPrice = NumberFormat.getNumberInstance().format(menu.price)
            binding.menuPrice.text = formattedPrice + "원"
            if (menu.id != null) {
                val imageUrl = "http://10.100.103.24:8080/ROOT/api/menu/menuimage/${menu.id}?timestamp=${System.currentTimeMillis()}"
                Log.d("Image URL111 : ", imageUrl)

                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .into(binding.menuImage)

                // isAvailable이 0이면 false, 1이면 true로 처리
                val isAvailable = menu.isAvailable

                // 품절 상태에 따른 UI 변경
                if (menu.isAvailable == 1) {
                    binding.soldOutimage.visibility = View.GONE // 품절 아님
                    binding.root.isEnabled = true              // 클릭 가능
                    binding.root.alpha = 1.0f
                    binding.menuImage.alpha = 1.0f
                } else {
                    binding.soldOutimage.visibility = View.VISIBLE // 품절 표시
                    binding.root.isEnabled = false                // 클릭 불가능
                    binding.menuImage.alpha = 0.5f
                }


                //클릭시 카트에담기  (품절이면 클릭안되게)
                binding.root.setOnClickListener {
                    if (menu.isAvailable == 1) {
                        binding.root.context?.let { context ->
                            menuViewModel.addToCart(menu, context)
                        }
                    }
                }
            }
        }
    }
}