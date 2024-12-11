package com.example.neworderapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.neworderapp.R
import com.example.neworderapp.dto.OrderedMenuResponse
import java.text.NumberFormat
import java.util.Locale

class OrderHistoryAdapter(private val orderHistoryList: List<OrderedMenuResponse>) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_orderhistory, parent, false)
        return OrderHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val item = orderHistoryList[position]
        holder.menuNameTextView.text = item.menuName // 예시로 "menuName"이라고 가정
        holder.quantityTextView.text = "${item.orderQuantity}"

        val formattedPrice = NumberFormat.getNumberInstance(Locale.KOREA).format(item.menuPrice)
        holder.priceTextView.text = "\u20A9${formattedPrice}"
    }

    override fun getItemCount(): Int = orderHistoryList.size

    class OrderHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuNameTextView: TextView = itemView.findViewById(R.id.menuNameTextView)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantityTextView)
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
    }
}