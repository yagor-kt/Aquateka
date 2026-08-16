package com.subefu.aquateka.view.utils

import androidx.recyclerview.widget.DiffUtil
import com.subefu.aquateka.model.domain.model.VisitWithClient

class VisitDiffCallback(
    private val oldList: List<VisitWithClient>,
    private val newList: List<VisitWithClient>,
): DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].visit.id == newList[newItemPosition].visit.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
