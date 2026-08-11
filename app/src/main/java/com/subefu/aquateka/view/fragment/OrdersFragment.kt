package com.subefu.aquateka.view.fragment

import android.annotation.SuppressLint
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
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.subefu.aquateka.databinding.FragmentOrdersBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.activity.CreateOrderActivity
import com.subefu.aquateka.view.adapter.OrdersCardAdapter
import com.subefu.aquateka.viewmodel.MainViewModel
import com.subefu.aquateka.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
@RequiresApi(Build.VERSION_CODES.O)
class OrdersFragment : Fragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: MainViewModel by activityViewModels {
        val dataBase = DataBase.getDB(requireContext().applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        MainViewModelFactory(repository)
    }

    private lateinit var rvAdapter: OrdersCardAdapter
    var currantDay = LocalDate.now()
    var currentVisits = listOf<VisitWithClient>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        binding.fabAddOrder.setOnClickListener {
            val intent = Intent(requireContext(), CreateOrderActivity::class.java)
            intent.putExtra("type", "create")
            startActivity(intent)
        }

        binding.searchLayout.editText?.doOnTextChanged {text, _, _, _ ->
            //TODO("реализовать условия поиска")
            val newList = currentVisits.filter {
                it.client.name.lowercase().contains(text.toString().lowercase())
            }
            rvAdapter.updateList(newList)

            //TODO(Сделать расчет по данным списка)
            updateShortInfo(newList)
        }

        //TODO("сделать экспорт/импорт")
        binding.imExport.setOnClickListener {
            Toast.makeText(requireContext(), "Экспорт в разработке", Toast.LENGTH_SHORT).show()
            //TODO(Сделать экспорт заказов по текущему месяцу)
        }
        binding.imImport.setOnClickListener {
            Toast.makeText(requireContext(), "Импорт в разработке", Toast.LENGTH_SHORT).show()
            //TODO(Сделать импорт заказов по текущему месяцу)
        }

        //setup current date, edit date after change in layout
        binding.imMonthPrevious.setOnClickListener {
            currantDay = currantDay.plusMonths(1)
            updateMonthInfo(currantDay)
        }
        binding.imMonthNext.setOnClickListener {
            currantDay = currantDay.minusMonths(1)
            updateMonthInfo(currantDay)
        }
    }


    fun setupUI(){
        rvAdapter = OrdersCardAdapter(
            emptyList(),
            onItemClick = { item ->
                val bottomSheet = OrderInfoFragment.newInstance(
                    //TODO(сюда передать объект order, реализовать у него parcelable)
                    title = item.client.name,
                    description = "some desc"
                )
                bottomSheet.show(childFragmentManager, "MyBottomSheetDialog")
            },
            onLongItemClick = { item ->
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
            }
        )

        binding.rvOrders.apply{
            itemAnimator = null
            adapter = rvAdapter
        }

        binding.tvMonth.apply {
            updateMonthInfo(currantDay)
        }
    }

    //обновляем поля и запрашиваем новый месяц через view model
    fun updateMonthInfo(date: LocalDate){
        binding.tvMonth.text = date.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        binding.tvYear.text = date.year.toString()

        sharedViewModel.loadVisits(date.monthValue, date.year)
    }

    fun updateShortInfo(visits: List<VisitWithClient>){
        val all = visits.size
        val completed = visits.filter { it.visit.status == "COMPLETED" }.size
        val moved = visits.filter { it.visit.status == "POSTPONED" }.size

        binding.apply {
            tvAll.text = "Всего: $all"
            tvCompleted.text = "Завершенных: $completed"
            tvMoved.text = "Перенесено: $moved"
        }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage", "NewApi")
    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED){
                sharedViewModel.visits.collect{ visits ->
                    rvAdapter.updateList(
                        visits.map{
                            it.copy(visit = it.visit.copy(), client = it.client.copy())
                        }
                    )
                    currentVisits = visits
                    updateShortInfo(visits)
                    Log.d("MyDB", visits.toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
