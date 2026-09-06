package com.subefu.aquateka.view.adapter

import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.CardOrderLayoutBinding
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.utils.VisitDiffCallback
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VisitCardAdapter(
    var orders: List<VisitWithClient>,
    val onItemClick: (VisitWithClient) -> Unit,
    val onLongItemClick: (VisitWithClient) -> Unit,
):
    RecyclerView.Adapter<VisitCardAdapter.OrdersCardViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersCardViewHolder {
        val binding = CardOrderLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrdersCardViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: OrdersCardViewHolder, position: Int) {
//        Log.d("MyAdapter", "bind")
        holder.bind(orders[position])
    }

    override fun getItemCount() = orders.size

    fun updateList(newList: List<VisitWithClient>){
        /*Log.d("MyAdapter", "newList $newList")
        Log.d("MyAdapter", "oldList $orders")*/
        val diffCallback = VisitDiffCallback(newList = newList, oldList = orders)
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
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: VisitWithClient) {
            binding.apply {
                tvName.text = item.client.name
                tvDate.text =
                    if(item.visit.actual_date == 0)
                        "——"
                    else
                        LocalDate.ofEpochDay(item.visit.actual_date.toLong()).format(DateTimeFormatter.ofPattern("dd.MM.yy"))
                tvPhone.text = item.client.phone.split(",").onEach { it -> it.trim() }.joinToString("\n")
                tvPrice.text = "${item.visit.price}₽"

                tvStatus.text = when(item.visit.status){
                    MyConst.COMPLETED -> MyConst.SHORT_COMPLETED
                    MyConst.POSTPONED -> MyConst.SHORT_POSTPONED
                    MyConst.PLANNED -> MyConst.SHORT_PLANNED
                    MyConst.RESCHEDULE_FROM_PAST -> MyConst.SHORT_RESCHEDULE_FROM_PAST
                    MyConst.COMPLETED -> MyConst.SHORT_COMPLETED
                    else -> ""
                }

                tvCoordinate.text = "${item.visit.latitude}/${item.visit.longitude}"

                ivStatusColor.imageTintList = when(item.visit.status){
                    MyConst.COMPLETED -> getColor(R.color.green)
                    MyConst.POSTPONED -> getColor(R.color.blue)
                    MyConst.POSTPONED -> getColor(R.color.blue)
                    MyConst.RESCHEDULE_FROM_PAST -> getColor(R.color.magenta)
                    MyConst.PLANNED -> getColor(R.color.orange)
                    else -> getColor(R.color.dark_surface)
                }
            }
        }

        fun getColor(colorId: Int): ColorStateList? = ContextCompat.getColorStateList(binding.root.context, colorId)
    }
}