package com.subefu.aquateka.view.utils

import androidx.recyclerview.widget.DiffUtil
import com.subefu.aquateka.model.domain.model.Client

class ClientDiffCallback(
    private val oldList: List<Client>,
    private val newList: List<Client>,
): DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].clietn_id == newList[newItemPosition].clietn_id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}