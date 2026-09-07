package com.subefu.aquateka.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.subefu.aquateka.App
import com.subefu.aquateka.databinding.FragmentOrdersBinding
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.utill.ImportExportState
import com.subefu.aquateka.view.activity.CreateVisitActivity
import com.subefu.aquateka.view.adapter.VisitCardAdapter
import com.subefu.aquateka.view.utils.TopBottomPaddingDecoration
import com.subefu.aquateka.view.utils.VisitCardAdapterFactory
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import com.subefu.aquateka.viewmodel.MainViewModel
import com.subefu.aquateka.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class VisitsFragment : Fragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels {
        MainViewModelFactory(App.repository)
    }

    private val viewModel: EditItemViewModel by activityViewModels {
        EditItemViewModelFactory(App.repository)
    }

    private lateinit var rvAdapter: VisitCardAdapter
    private var currantDay = LocalDate.now()

    private var currentVisits = listOf<VisitWithClient>()
    private var currentUnapprovedVisits = listOf<Visit>()

    private var isPostponedOnDate = false

    private var csvTextToWrite = ""
    private val createCsvLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/comma-separated-values")
    ) { uri ->
        uri?.let { saveCsvToUri(it, csvTextToWrite) }
    }
    private val importCsvLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importCsvFromUri(it) }
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

        sharedViewModel.unapprovedVisits
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { visitsPayload ->
                Log.d("MyVisitsF", "unapproved visits: $visitsPayload")
                if (visitsPayload?.visits.isNullOrEmpty().not()) {
                    if(isPostponedOnDate)
                        Log.d("MyVisitF", "ложное срабатывание переноса")
                    else{
                        isPostponedOnDate = true
                        currentUnapprovedVisits = visitsPayload.visits
                    }
                }
            }
            .launchIn(lifecycleScope)

        sharedViewModel.importState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .filterNotNull()
            .onEach { state ->
                when(state){
                    is ImportExportState.SuccessImportVisits -> {
                        importVisits(state.visits)
                    }
                    is ImportExportState.SuccessExportVisits -> {
                        exportVisits(state.cvs)
                    }
                    else -> {}
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.fabAddOrder.setOnClickListener {
            val intent = Intent(requireContext(), CreateVisitActivity::class.java)
            intent.putExtra(MyConst.TYPE, MyConst.CREATE)
            startActivity(intent)
        }

        binding.btCheckIsPostpone.setOnClickListener {
            if(currentUnapprovedVisits.isNotEmpty() && isPostponedOnDate)
                askPostponeUnapprovedVisit(currentUnapprovedVisits)
            else
                AppEventBus.post(AppMessage.Success("Нет визитов для переноса"))
        }
    }

    fun importVisits(visits: List<Visit>){
        if (visits.isNotEmpty()) {
            viewModel.insertVisits(visits)
            sharedViewModel.resetImportState()
            AppEventBus.post(AppMessage.Success("Импортировано ${visits.size} записей визитов"))
        } else {
            AppEventBus.post(AppMessage.Error("Нет данных для импорта"))
        }
        binding.imImport.isClickable = true
    }

    fun exportVisits(csvContent: String){
        Log.d("MyVisitF", "csv: $csvContent")
        csvTextToWrite = csvContent
        val month = binding.tvMonth.text.toString()
        val year = binding.tvYear.text.toString()
        createCsvLauncher.launch("visits_${month}_${year}.csv")
        sharedViewModel.resetImportState()
        binding.imExport.isClickable = true
    }


    @SuppressLint("ClickableViewAccessibility")
    fun setupUI(){
        setResetFocus()

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
        ){ visitWithClient, mode ->
            if (mode == MyConst.APPROVE) {
                sharedViewModel.postponeVisit(null, mode, visitWithClient.visit)
                return@getInstance
            }
            else if (mode == MyConst.MANUAL_POSTPONE) {
                selectPostponeMonth{ date ->
                    sharedViewModel.postponeVisit(
                        if (mode == MyConst.MANUAL_POSTPONE)
                            date
                        else null,
                        mode,
                        visitWithClient.visit,
                    )
                }
            }
        }

        binding.rvOrders.apply{
            adapter = rvAdapter
            addItemDecoration(TopBottomPaddingDecoration(10, 10))
        }
    }

    fun selectPostponeMonth(
        dateSetListener: (Pair<Int, Int>) -> Unit
    ){
        val year = currantDay.year
        val month = currantDay.monthValue
        val day = currantDay.dayOfMonth

        DatePickerDialog(requireContext(), 0, {_,selectedYear,selectedMonth,selectedDay ->
            val date = Pair(selectedMonth + 1, selectedYear)
            dateSetListener(date)
        }, year, month-1, day).show()
    }

    fun setupExportImport(){
        binding.imExport.setOnClickListener {
            binding.imExport.isClickable = false
            val visits = currentVisits
            sharedViewModel.generateCsvData(visits)
        }
        binding.imImport.setOnClickListener {
            binding.imImport.isClickable = false
            importCsvLauncher.launch(arrayOf("text/comma-separated-values", "text/csv"))
        }
    }

    fun setupDateChange(){
        //setup current date, edit date after change in layout
        binding.imMonthNext.setOnClickListener {
            currantDay = currantDay.plusMonths(1)
            updateMonthInfo(currantDay)
        }
        binding.imMonthPrevious.setOnClickListener {
            currantDay = currantDay.minusMonths(1)
            updateMonthInfo(currantDay)
        }

        binding.dateContainer.setOnClickListener {
            val year = currantDay.year
            val month = currantDay.monthValue
            val day = currantDay.dayOfMonth

            DatePickerDialog(requireContext(), 0, {_,selectedYear,selectedMonth,selectedDay ->
                Log.d("MyVisits", "$selectedYear,$selectedMonth,$selectedDay")
                currantDay = LocalDate.of(selectedYear, selectedMonth+1, selectedDay)
                updateMonthInfo(currantDay)
            }, year, month-1, day).show()
        }
    }

    fun askPostponeUnapprovedVisit(unapprovedVisits: List<Visit>){
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Перенести невыполненные визиты?")
            .setMessage("Найдено \"${unapprovedVisits.size}\" неперенесенных визитов. Выполнить автоперенос на текущий месяц?")
            .setPositiveButton("Да") { witch, _ ->
                Log.d("MyVisit", "selected auto postpone visits")
                sharedViewModel.postponeVisit(null, MyConst.AUTOMATIC_POSTPONE, *unapprovedVisits.toTypedArray())
                currentUnapprovedVisits = listOf()
                witch.cancel()
            }
            .setNeutralButton("Нет") { witch, _ ->
                witch.cancel()
            }
        dialog.show()
    }

    //обновляем поля и запрашиваем новый месяц через view model
    fun updateMonthInfo(date: LocalDate){
        if (date == currentVisits) return
        binding.tvMonth.text = date.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
        binding.tvYear.text = date.year.toString()

        isPostponedOnDate = false
        sharedViewModel.setCurrentDate(date.monthValue, date.year)
    }

    fun updateShortInfo(visits: List<VisitWithClient>){
        val all = visits.size
        val completed = visits.filter { it.visit.status == MyConst.COMPLETED }.size
        val moved = visits.filter { it.visit.status == MyConst.RESCHEDULE_FROM_PAST }.size

        binding.apply {
            tvAll.text = "Всего: $all"
            tvCompleted.text = "Завершенных: $completed"
            tvMoved.text = "Перенесено: $moved"
        }
    }

    fun saveCsvToUri(uri: Uri, content: String) {
        if (!isAdded) return // Защита от утечки памяти, если пользователь закрыл экран
        runCatching {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
            }
            AppEventBus.post(AppMessage.Success("Файл успешно сохранен!"))
        }
            .onFailure { e ->
                AppEventBus.post(AppMessage.Error("Ошибка сохранения: ${e.message}"))
            }
    }

    fun importCsvFromUri(uri: Uri) {
        if (!isAdded) return
        runCatching {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val csvContent = inputStream.bufferedReader(Charsets.UTF_8).readText()
                sharedViewModel.parseCsvToVisits(csvContent)
                AppEventBus.post(AppMessage.Success("Файл имортирован успешно"))
            }
        }
            .onFailure { e ->
                AppEventBus.post(AppMessage.Error("Ошибка импорта: ${e.message}"))
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setResetFocus(){
        val setMainFocus = {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
            binding.main.requestFocus()
        }

        binding.main.setOnTouchListener { _, _ ->
            if (binding.etSearch.hasFocus()) {
                setMainFocus.invoke()
            }
            false
        }

        binding.rvOrders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if(newState == RecyclerView.SCROLL_STATE_DRAGGING ) {
                    setMainFocus.invoke()
                }
            }
        }
        )
    }

    override fun onResume() {
        super.onResume()
        binding.imExport.isClickable = true
        binding.imImport.isClickable = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
