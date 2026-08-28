package com.subefu.aquateka.view.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.adapter.VisitCardAdapter
import com.subefu.aquateka.view.fragment.VisitInfoFragment

object VisitCardAdapterFactory {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getInstance(
        visits: List<VisitWithClient>,
        context: Context,
        fragmentManager: FragmentManager
    ) = VisitCardAdapter(
        visits,
        onItemClick = { item ->
            val bottomSheet = VisitInfoFragment.newInstance(visit = item)
            bottomSheet.show(fragmentManager, "MyBottomSheetDialog")
        },
        onLongItemClick = { item ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Завершить заказ?")
                .setNegativeButton("Перенести"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    //TODO(завершение заказа, след дата = текущая + период)
                }
                .setNeutralButton("Выбрать месяц", {dialog, witch ->
                    dialog.cancel()
                })
            builder.show()
        }
    )
}