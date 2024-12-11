package com.example.neworderapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.neworderapp.data.Category
import com.example.neworderapp.databinding.ItemCategoryBinding
import com.example.neworderapp.viewmodel.MenuViewModel

//메인에서 카데고리를 표현하기위한 어댑터
class CategoryAdapter(
    private var categories: List<Category>,
    private val menuViewModel: MenuViewModel,
    private val context: Context,
    private val storeId: String // storeId를 추가
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding, menuViewModel, parent.context,storeId)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val menuViewModel: MenuViewModel,
        private val context: Context ,
        private val storeId: String //
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(category: Category) {
            binding.categoryName.text = category.cname

            // 메뉴 어댑터 설정
            val menuAdapter = MenuAdapter(emptyList(), menuViewModel) // MenuAdapter에 ViewModel 전달


            binding.menuRecyclerView.adapter = menuAdapter
            binding.menuRecyclerView.layoutManager = LinearLayoutManager(context)

            // GridLayoutManager로 3개의 열로 배치
            val gridLayoutManager = GridLayoutManager(binding.root.context, 3)
            binding.menuRecyclerView.layoutManager = gridLayoutManager

            // 카테고리에 해당하는 메뉴 데이터 가져오기
            menuViewModel.getMenusByStoreAndCategory(storeId,category.categoryId).observeForever { menus ->
                menuAdapter.updateMenus(menus)
            }
        }
    }
}