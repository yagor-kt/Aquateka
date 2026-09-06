package com.subefu.aquateka.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.size
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.subefu.aquateka.App
import com.subefu.aquateka.databinding.FragmentCustomersBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.utill.ImportExportState
import com.subefu.aquateka.view.activity.CreateClientActivity
import com.subefu.aquateka.view.activity.ProfileClientActivity
import com.subefu.aquateka.view.adapter.ClientCardAdapter
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import com.subefu.aquateka.viewmodel.MainViewModel
import com.subefu.aquateka.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.getValue
import com.subefu.aquateka.model.domain.utill.ImportExportState.*
import com.subefu.aquateka.view.utils.TopBottomPaddingDecoration

@RequiresApi(Build.VERSION_CODES.O)
class ClientsFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels {
        MainViewModelFactory(App.repository)
    }
    private val viewModel: EditItemViewModel by activityViewModels {
        EditItemViewModelFactory(App.repository)
    }

    private lateinit var rvAdapter: ClientCardAdapter
    private var clients = listOf<Client>()

    private var csvTextVisitsToWrite = ""
    private var csvTextToClientsWrite = ""
    private val createCsvVisitLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/comma-separated-values")) { uri ->
        uri?.let { saveCsvToUri(it, csvTextVisitsToWrite) }
    }
    private val createCsvClientLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/comma-separated-values")) { uri ->
        uri?.let { saveCsvToUri(it, csvTextToClientsWrite ) }
    }
    private var isClient = true
    private var importClientIds = listOf<Int>()
    private val importCsvVisitLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importCsvFromUri(it, false) }
    }
    private val importCsvClientLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importCsvFromUri(it, true)}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRV()
        setupExportImport()

        sharedViewModel.clients
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { clients ->
                this.clients = clients
                rvAdapter.updateList(clients)
                updateShortInfo(clients)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        sharedViewModel.importState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .filterNotNull()
            .onEach { state ->
                when(state){
                    is SuccessImportClients -> {
                        importClients(state.visits)
                    }
                    is SuccessImportVisits -> {
                        importVisits(state.visits)
                    }
                    is SuccessExportClients -> {
                        exportClients(state.cvs)
                    }
                    is SuccessExportVisits -> {
                        exportVisits(state.cvs)
                    }
                    is Error -> {
                        AppEventBus.post(AppMessage.Error(state.message))
                    }
                    else -> {}
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.fabAddCustomer.setOnClickListener {
            val intent = Intent(requireContext(), CreateClientActivity::class.java)
            intent.putExtra(MyConst.TYPE, MyConst.CREATE)
            startActivity(intent)
        }
    }

    fun importClients(clients: List<Client>){
        if (clients.isNotEmpty()) {
            viewModel.insertClients(clients)
            //сохраняем id импортируемых клиентов для импорта только их визитов
            importClientIds = clients.map { it.clietn_id }
            sharedViewModel.resetImportState()
            AppEventBus.post(AppMessage.Success("Импортировано ${clients.size} записей киентов -К"))
            //импорируем визиты клиентов
            isClient = false
            importCsvVisitLauncher.launch(arrayOf("text/comma-separated-values", "text/csv"))
        } else {
            AppEventBus.post(AppMessage.Error("Нет данных для импорта"))
        }
        binding.imImport.isClickable = true
    }

    fun exportClients(csvContent: String){
        Log.d("MyVisitF", "csv clients export: $csvContent")
        csvTextToClientsWrite = csvContent
        val month = LocalDate.now().monthValue
        val year = LocalDate.now().year
        createCsvClientLauncher.launch("clients_${month}_${year}.csv")
        sharedViewModel.resetImportState()
        binding.imExport.isClickable = true

        lifecycleScope.launch {
            AppEventBus.post(AppMessage.Success("Экспорт визитов. не закрывайте приложение"))
            val visits = sharedViewModel.getVisitsByClientIds(clients.map { it.clietn_id })
            Log.d("MyClientF", "visits by ids export size: ${visits.size}")
            sharedViewModel.generateCsvData(visits)
            AppEventBus.post(AppMessage.Success("Экспорт визитов завершен"))
        }
    }

    fun exportVisits(csvContent: String){
        Log.d("MyVisitF", "csv visit export: $csvContent")
        val month = LocalDate.now().monthValue
        val year = LocalDate.now().year
        csvTextVisitsToWrite = csvContent
        createCsvVisitLauncher.launch("visits_${month}_${year}.csv")
        sharedViewModel.resetImportState()
        binding.imExport.isClickable = true
    }

    fun importVisits(visits: List<Visit>){
        if (visits.isNotEmpty()) {
            // импортируем только импортных клиентов
            viewModel.insertVisits(visits.filter { it.clientId in importClientIds})
            sharedViewModel.resetImportState()
            AppEventBus.post(AppMessage.Success("Импортировано ${visits.size} записей визитов -К"))
        } else {
            AppEventBus.post(AppMessage.Error("Нет данных для импорта"))
        }
        binding.imImport.isClickable = true
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupUI(){
        //сброс фокуса строки поиска при клике за ее пределами
        setResetFocus()

        binding.searchLayout.editText?.doOnTextChanged {text, _, _, _ ->
            val searchText = text.toString().trim()
            val newList = clients.filter {
                it.name.contains(searchText, true) ||
                        it.phone.contains(searchText, true)
            }
            rvAdapter.updateList(newList)
            updateShortInfo(newList)
        }
    }

    fun setupRV(){
        rvAdapter = ClientCardAdapter(
            clients = clients,
            onItemClick = { item ->
                val intent = Intent(requireContext(), ProfileClientActivity::class.java)
                intent.putExtra(MyConst.CLIENT_ID, item.clietn_id)
                startActivity(intent)
            })

        binding.rvCustomers.apply{
            adapter = rvAdapter
            Log.d("MyLog", this.size.toString())
            addItemDecoration(TopBottomPaddingDecoration(10, 100))
        }
    }

    fun setupExportImport(){
        binding.imExport.setOnClickListener {
            binding.imExport.isClickable = false
            val clients = clients
            sharedViewModel.generateCsvData(clients)
        }
        binding.imImport.setOnClickListener {
            binding.imImport.isClickable = false
            isClient = true
            importCsvClientLauncher.launch(arrayOf("text/comma-separated-values", "text/csv"))
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

    fun importCsvFromUri(uri: Uri, isClients: Boolean) {
        if (!isAdded) return
        runCatching {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val csvContent = inputStream.bufferedReader(Charsets.UTF_8).readText()
                Log.d("MyClientF", "is: $isClient // importCsvFromUri: $csvContent")
                if(isClients) sharedViewModel.parseCsvToClients(csvContent)
                else sharedViewModel.parseCsvToVisits(csvContent)
                AppEventBus.post(AppMessage.Success("Файл имортирован успешно"))
            }
        }
            .onFailure { e ->
                AppEventBus.post(AppMessage.Error("Ошибка импорта: ${e.message}"))
            }
    }

    fun updateShortInfo(visits: List<Client>){
        val all = visits.size
        binding.apply {
            tvAll.text = "Всего: $all"
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

        binding.rvCustomers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        binding.imImport.isClickable = true
        binding.imExport.isClickable = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}