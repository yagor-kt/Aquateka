package com.subefu.aquateka.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.subefu.aquateka.databinding.CardCustomerLayoutBinding
import com.subefu.aquateka.view.utils.MyDiffCallback

class CustomersCardAdapter(
    var customers: List<String>,
    val onItemClick: (String) -> Unit,
): RecyclerView.Adapter<CustomersCardAdapter.CustomersCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomersCardViewHolder {
        val binding = CardCustomerLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CustomersCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomersCardViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = customers.size

    fun updateList(newList: List<String>){
        //todo(сделать другой diffUtill)
        val diffCallback = MyDiffCallback(newList, customers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        customers = newList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class CustomersCardViewHolder(val binding: CardCustomerLayoutBinding)
        : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            val item = customers[position]

            binding.apply {
                //todo(заполнить карточку здесь)
                tvName.text = item


                root.setOnClickListener {
                    val actualPos = super.getBindingAdapterPosition()
                    if(actualPos != position)
                        onItemClick(item)
                    else
                        onItemClick(item)
                }
            }
        }

    }
}