package com.subefu.aquateka.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.subefu.aquateka.databinding.CardOrderLayoutBinding
import com.subefu.aquateka.view.utils.MyDiffCallback

class OrdersCardAdapter(
    var orders: List<String>,
    val onItemClick: (String) -> Unit,
    val onLongItemClick: (String) -> Unit,
):
    RecyclerView.Adapter<OrdersCardAdapter.OrdersCardViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersCardViewHolder {
        val binding = CardOrderLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrdersCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersCardViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = orders.size

    fun updateList(newList: List<String>){
        val diffCallback = MyDiffCallback(newList, orders)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        orders = newList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class OrdersCardViewHolder(val binding: CardOrderLayoutBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            binding.tvName.text = orders[position]
            binding.root.setOnClickListener{
                val actualPos = this.bindingAdapterPosition
                if(actualPos != position)
                    onItemClick(orders[actualPos])
                else
                    onItemClick(orders[position])
            }
            binding.root.setOnLongClickListener{
                val actualPos = this.bindingAdapterPosition
                if(actualPos != position)
                    onLongItemClick(orders[actualPos])
                else
                    onLongItemClick(orders[position])
                false
            }
        }
    }
}