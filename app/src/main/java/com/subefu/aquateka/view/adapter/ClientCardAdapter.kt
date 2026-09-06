package com.subefu.aquateka.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.subefu.aquateka.databinding.CardCustomerLayoutBinding
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.view.utils.ClientDiffCallback

class ClientCardAdapter(
    var clients: List<Client>,
    val onItemClick: (Client) -> Unit,
): RecyclerView.Adapter<ClientCardAdapter.CustomersCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomersCardViewHolder {
        val binding = CardCustomerLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CustomersCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomersCardViewHolder, position: Int) {
        holder.bind(clients[position])
    }

    override fun getItemCount() = clients.size

    fun updateList(newList: List<Client>){
        val diffCallback = ClientDiffCallback(newList = newList, oldList = clients)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        clients = newList
        diffResult.dispatchUpdatesTo(this)
    }

    inner class CustomersCardViewHolder(val binding: CardCustomerLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val actualPos = bindingAdapterPosition
                if(actualPos != RecyclerView.NO_POSITION)
                    onItemClick(clients[actualPos])
            }
        }

        fun bind(client: Client){
            binding.apply {
                tvName.text = client.name
                tvAddress.text = client.address ?: "адрес не указан"
                tvCoordinate.text = "${client.latitude}/${client.longitude}"
                tvPhone.text = client.phone.split(",").onEach { it.trim() }.joinToString("\n")
            }
        }
    }
}