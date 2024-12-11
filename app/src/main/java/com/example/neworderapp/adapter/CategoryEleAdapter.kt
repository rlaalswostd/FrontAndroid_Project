package com.example.neworderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.neworderapp.R
import com.example.neworderapp.data.Category
import com.example.neworderapp.databinding.ItemCategoryBtnBinding

class CategoryEleAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category,Int) -> Unit // 클릭 이벤트 처리
) : RecyclerView.Adapter<CategoryEleAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryButton: Button = itemView.findViewById(R.id.categoryButton)

        fun bind(category: Category, position: Int) {
            categoryButton.text = category.cname  // 버튼 텍스트 설정
            categoryButton.setOnClickListener {
                onCategoryClick(category,position)  // 버튼 클릭 시 카테고리 정보 전달
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_btn, parent, false)
        return ViewHolder(view)


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    override fun getItemCount(): Int = categories.size


    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged() // UI 갱신
    }
}