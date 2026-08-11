package com.subefu.aquateka.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.subefu.aquateka.databinding.CardOrderLayoutBinding
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.utils.MyDiffCallback

class OrdersCardAdapter(
    var orders: List<VisitWithClient>,
    val onItemClick: (VisitWithClient) -> Unit,
    val onLongItemClick: (VisitWithClient) -> Unit,
):
    RecyclerView.Adapter<OrdersCardAdapter.OrdersCardViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersCardViewHolder {
        val binding = CardOrderLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrdersCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersCardViewHolder, position: Int) {
        Log.d("MyAdapter", "bind")
        holder.bind(orders[position])
    }

    override fun getItemCount() = orders.size

    fun updateList(newList: List<VisitWithClient>){
        Log.d("MyAdapter", "newList $newList")
        Log.d("MyAdapter", "oldList $orders")
        val diffCallback = MyDiffCallback(newList = newList, oldList = orders)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        orders = newList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class OrdersCardViewHolder(val binding: CardOrderLayoutBinding): RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val actualPos = bindingAdapterPosition
                if (actualPos != RecyclerView.NO_POSITION) {
                    onItemClick(orders[actualPos])
                }
            }
            binding.root.setOnLongClickListener {
                val actualPos = bindingAdapterPosition
                if (actualPos != RecyclerView.NO_POSITION) {
                    onLongItemClick(orders[actualPos])
                }
                true
            }
        }
        fun bind(item: VisitWithClient) {
            binding.apply {
                tvName.text = item.client.name
                tvDate.text = item.visit.actual_date.toString()
                tvPhone.text = item.client.phone
                tvPrice.text = item.visit.price.toString()
                tvStatus.text = item.visit.status
            }
        }
    }
}