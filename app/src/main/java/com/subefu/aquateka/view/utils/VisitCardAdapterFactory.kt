package com.subefu.aquateka.view.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.adapter.VisitCardAdapter
import com.subefu.aquateka.view.fragment.VisitInfoFragment

object VisitCardAdapterFactory {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getInstance(
        visits: List<VisitWithClient>,
        context: Context,
        fragmentManager: FragmentManager,
        onClickListener: (VisitWithClient, String) -> Unit
    ) = VisitCardAdapter(
        visits,
        onItemClick = { item ->
            val bottomSheet = VisitInfoFragment.newInstance(visit = item)
            bottomSheet.show(fragmentManager, "MyBottomSheetDialog")
        },
        onLongItemClick = { item ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Завершить визит?")
                .setPositiveButton("ДА"){dialog, witch ->
                    onClickListener(item, MyConst.APPROVE)
                    dialog.cancel()
                }
                .setNegativeButton("Перенести вручную"){dialog, witch ->
                    onClickListener(item, MyConst.MANUAL_POSTPONE)
                    dialog.cancel()
                }
                .setNeutralButton("Отмена", {dialog, witch ->
                    dialog.cancel()
                })
            builder.show()
        }
    )
}