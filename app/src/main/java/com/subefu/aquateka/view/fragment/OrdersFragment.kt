package com.subefu.aquateka.view.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.FragmentCustomersBinding
import com.subefu.aquateka.databinding.FragmentOrdersBinding
import com.subefu.aquateka.view.activity.CreateOrderActivity
import com.subefu.aquateka.view.adapter.OrdersCardAdapter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class OrdersFragment : Fragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

        private lateinit var rvAdapter: OrdersCardAdapter
        val list = listOf("Alex", "Bruno", "Flew", "Uvritulatuhalse")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var currantDay = LocalDate.now()

        rvAdapter = OrdersCardAdapter(list, onItemClick = {
            item ->
            val bottomSheet = OrderInfoFragment.newInstance(
                //TODO(сюда передать объект order, реализовать у него parcelable)
                title = item,
                description = "some desc"
            )
            bottomSheet.show(childFragmentManager, "MyBottomSheetDialog")
        }, onLongItemClick = {
            item ->
                val builder = AlertDialog.Builder(requireContext())
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
        })

        binding.rvOrders.apply{
            adapter = rvAdapter
            setHasFixedSize(true)
        }

        binding.fabAddOrder.setOnClickListener {
            //TODO(передать параметр "Создание" в заголовок страницы)
            //TODO(перекрасить цвет кнопок на странице)
            val intent = Intent(requireContext(), CreateOrderActivity::class.java)
            intent.putExtra("type", "create")
            startActivity(intent)
        }

        binding.searchLayout.editText?.doOnTextChanged {text, _, _, _ ->
            val newList = list.filter { it.contains(text.toString()) }
            rvAdapter.updateList(newList)

            //TODO(Сделать расчет по данным списка)
            updateShortInfo(
                newList.size,
                newList.filter { it.endsWith("o") }.size,
                newList.filter { it.startsWith("A") }.size
            )
        }

        binding.imExport.setOnClickListener {
            Toast.makeText(requireContext(), "Экспорт в разработке", Toast.LENGTH_SHORT).show()
            //TODO(Сделать экспорт заказов по текущему месяцу)
        }
        binding.imImport.setOnClickListener {
            Toast.makeText(requireContext(), "Импорт в разработке", Toast.LENGTH_SHORT).show()
            //TODO(Сделать импорт заказов по текущему месяцу)
        }


        //ser month
        binding.tvMonth.apply {
            text = currantDay.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        }
        binding.imMonthPrevious.setOnClickListener {
            val previousMonth = currantDay.plusMonths(1)
            currantDay = previousMonth
            binding.tvMonth.text = previousMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        }
        binding.imMonthNext.setOnClickListener {
            val nextMonth = currantDay.minusMonths(1)
            currantDay = nextMonth
            binding.tvMonth.text = nextMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        }
    }

    fun updateShortInfo(all: Int, completed: Int, moved: Int){
        binding.tvAll.text = "Всего: $all"
        binding.tvCompleted.text = "Завершенных: $completed"
        binding.tvMoved.text = "Перенесено: $moved"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
