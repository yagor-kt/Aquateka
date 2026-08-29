package com.subefu.aquateka.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.subefu.aquateka.databinding.FragmentOrdersBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.activity.CreateVisitActivity
import com.subefu.aquateka.view.adapter.VisitCardAdapter
import com.subefu.aquateka.view.utils.VisitCardAdapterFactory
import com.subefu.aquateka.viewmodel.MainViewModel
import com.subefu.aquateka.viewmodel.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
@RequiresApi(Build.VERSION_CODES.O)
class VisitsFragment : Fragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels {
        val dataBase = DataBase.getDB(requireContext().applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        MainViewModelFactory(repository)
    }

    private lateinit var rvAdapter: VisitCardAdapter
    private var currantDay = LocalDate.now()
    private var currentVisits = listOf<VisitWithClient>()

    private var csvTextToWrite = ""
    private val createCsvLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/comma-separated-values")
    ) { uri ->
        uri?.let { saveCsvToUri(it, csvTextToWrite) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRV()
        setupDateChange()
        setupExportImport()

        sharedViewModel.visits
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { visits ->
                rvAdapter.updateList(visits)
                currentVisits = visits
                updateShortInfo(visits)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.fabAddOrder.setOnClickListener {
            val intent = Intent(requireContext(), CreateVisitActivity::class.java)
            intent.putExtra(MyConst.TYPE, MyConst.CREATE)
            startActivity(intent)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun setupUI(){
        updateMonthInfo(currantDay)
        updateShortInfo(currentVisits)

        //сброс фокуса строки поиска
        binding.main.setOnTouchListener { _, _ ->
            if (binding.etSearch.hasFocus()) {
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                binding.main.requestFocus()
            }
            false
        }

        binding.searchLayout.editText?.doOnTextChanged {text, _, _, _ ->
            val searchText = text.toString().lowercase().trim()
            val newList = currentVisits.filter {
                it.client.name.contains(searchText, true) ||
                        it.client.phone.contains(searchText, true) ||
                        ("${it.client.latitude} ${it.client.longitude}").contains(searchText, true)
            }
            rvAdapter.updateList(newList)
            updateShortInfo(newList)
        }
    }

    fun setupRV(){
        rvAdapter = VisitCardAdapterFactory.getInstance(
            emptyList(),
            requireContext(),
            childFragmentManager,
        )

        binding.rvOrders.apply{
            adapter = rvAdapter
        }
    }

    fun setupExportImport(){
        //TODO("сделать экспорт/импорт")
        binding.imExport.setOnClickListener {
            val month = binding.tvMonth.text.toString()
            val year = binding.tvYear.text.toString()
            exportVisitsToCsv(month, year)
        }
        binding.imImport.setOnClickListener {
            Toast.makeText(requireContext(), "Импорт в разработке", Toast.LENGTH_SHORT).show()
            //TODO(Сделать импорт заказов по текущему месяцу)
        }
    }

    fun setupDateChange(){
        //setup current date, edit date after change in layout
        binding.imMonthPrevious.setOnClickListener {
            currantDay = currantDay.plusMonths(1)
            updateMonthInfo(currantDay)
        }
        binding.imMonthNext.setOnClickListener {
            currantDay = currantDay.minusMonths(1)
            updateMonthInfo(currantDay)
        }

        binding.dateContainer.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), 0, {_,selectedYear,selectedMonth,selectedDay ->
                Log.d("MyVisits", "$selectedYear,$selectedMonth,$selectedDay")
                sharedViewModel.setCurrentDate(selectedMonth+1, selectedYear)
                currantDay = LocalDate.of(selectedYear, selectedMonth+1, selectedDay)
                updateMonthInfo(currantDay)
            }, year, month, day).show()
        }
    }

    //обновляем поля и запрашиваем новый месяц через view model
    fun updateMonthInfo(date: LocalDate){
        binding.tvMonth.text = date.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        binding.tvYear.text = date.year.toString()

        sharedViewModel.setCurrentDate(date.monthValue, date.year)
    }

    fun updateShortInfo(visits: List<VisitWithClient>){
        val all = visits.size
        val completed = visits.filter { it.visit.status == MyConst.COMPLETED }.size
        val moved = visits.filter { it.visit.status == MyConst.POSTPONED }.size

        binding.apply {
            tvAll.text = "Всего: $all"
            tvCompleted.text = "Завершенных: $completed"
            tvMoved.text = "Перенесено: $moved"
        }
    }

    fun exportVisitsToCsv(month: String, year: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val visits = currentVisits
            val csvContent = sharedViewModel.generateCsvData(visits)

            withContext(Dispatchers.Main) {
                csvTextToWrite = csvContent
                // Открывает системное окно, где пользователь выберет папку "Загрузки" и введет имя файла
                createCsvLauncher.launch("visits_${month}_${year}.csv")
            }
        }
    }

    fun saveCsvToUri(uri: Uri, content: String) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
                Toast.makeText(context, "Файл успешно сохранен!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        updateMonthInfo(currantDay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
