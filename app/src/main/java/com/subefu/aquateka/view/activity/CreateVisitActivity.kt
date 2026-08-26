package com.subefu.aquateka.view.activity

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.subefu.aquateka.databinding.ActivityCreateOrderBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.model.domain.usecase.SetAddressVisitUseCase
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class CreateVisitActivity : AppCompatActivity() {

    private var _binding: ActivityCreateOrderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditItemViewModel by viewModels{
        val dataBase = DataBase.getDB(applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        val setAddressVisitUseCase = SetAddressVisitUseCase(repository)
        EditItemViewModelFactory(repository, setAddressVisitUseCase)
    }

    private val clientList = mutableListOf<Client>()
    private var currentClient: Client? = null
    private var currentVisit: Visit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityCreateOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()

        val type = intent.getStringExtra(MyConst.TYPE)
        if(type == MyConst.CREATE)
            binding.tvTitle.text = "Создание заказа"
        else {
            binding.tvTitle.text = "Редактирвоание заказа"
            setupEditData()
        }

        binding.tfName.editText?.doAfterTextChanged { view ->
            currentClient = clientList.find { it.name == binding.tfName.editText?.text.toString()}
//            Log.d("MyCOA", currentClient?.name.toString())
            currentClient?.let {
                binding.apply {
                    tfAddress.editText?.setText(it.address ?: "")
                    tfCoordinate.editText?.setText("${it.latitude},${it.longitude}")
                    tfPeriod.editText?.setText((it.period_month ?: 0).toString())
                }
            }
        }

        binding.btCancelled.setOnClickListener {
            val builder = getCancelledVisitDialog()
            builder.show()
        }

        binding.btPreserve.setOnClickListener {
            if(isNotFillData() || isNotValidateData()) return@setOnClickListener

            val builder = getPreserveVisitDialog()
            builder.show()
        }
    }

    fun setupEditData(){
        val visitWithClient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(MyConst.VISIT_WITH_CLIENT,
                VisitWithClient::class.java)
        } else {
            intent.getParcelableExtra(MyConst.VISIT_WITH_CLIENT)
        }

        if(visitWithClient == null) return

        val visit = visitWithClient.visit
        currentVisit = visit
        currentClient = visitWithClient.client

        fillUiVisitData(visit)
    }

    fun fillUiVisitData(visit: Visit){
        binding.apply {
            tfAddress.editText?.setText(visit.address)
            tfCoordinate.editText?.setText("${visit.latitude},${visit.longitude}")
            tfPlannedVisit.editText?.setText("1,${visit.planned_month},${visit.planned_year}")
            tfWorkType.editText?.setText(visit.work_type)
            tfPrice.editText?.setText(visit.price.toString())
            tfDetails.editText?.setText(visit.parts)
            tfComment.editText?.setText(visit.comment)
            tfPeriod.editText?.setText(visit.period.toString())
        }
    }

    fun getCancelledVisitDialog(): AlertDialog.Builder{
        return AlertDialog.Builder(this)
            .setTitle("Отменить ${binding.tvTitle.text}?")
            .setNegativeButton("НЕТ"){dialog, witch ->
                dialog.cancel()
            }
            .setPositiveButton("ДА"){dialog, witch ->
                dialog.cancel()
                this.finish()
            }
    }

    fun getPreserveVisitDialog(): AlertDialog.Builder{
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Сохранить заказ \"${currentClient?.name}\"?")
            .setNegativeButton("НЕТ"){dialog, witch -> dialog.cancel() }
            .setPositiveButton("ДА"){dialog, witch ->
                dialog.cancel()

                val visit = getVisitFromUI()

                if(currentVisit != null) {
                    if (visit.address.isNullOrBlank())
                        viewModel.updateVisit(visit, true)
                    else if (visit.address == "-")
                        viewModel.updateVisit(visit.copy(address = ""))
                    else
                        viewModel.updateVisit(visit)
                }
                else
                    viewModel.insertVisit(visit)
                this.finish()
            }
        return builder
    }

    fun getVisitFromUI(): Visit{
        return Visit(
            id = currentVisit?.id ?: 0,
            clientId = currentClient?.clietn_id ?: throw NullPointerException("Введите клиента"),
            address = binding.tfAddress.editText?.text.toString(),
            latitude = binding.tfCoordinate.editText?.text.toString().split(",").first().trim().toDoubleOrNull() ?: 0.0,
            longitude = binding.tfCoordinate.editText?.text.toString().split(",").last().trim().toDoubleOrNull() ?: 0.0,
            planned_month = binding.tfPlannedVisit.editText?.text.toString().split(",")[1].trim().toIntOrNull() ?: 0,
            planned_year = binding.tfPlannedVisit.editText?.text.toString().split(",")[2].trim().toIntOrNull() ?: 0,
            actual_date = currentVisit?.actual_date ?: 0,
            status = currentVisit?.status ?: MyConst.PLANNED,
            work_type = binding.tfWorkType.editText?.text.toString(),
            price = binding.tfPrice.editText?.text.toString().toIntOrNull() ?: 0,
            parts = binding.tfDetails.editText?.text.toString(),
            comment = binding.tfComment.editText?.text.toString(),
            period = binding.tfPeriod.editText?.text.toString().toIntOrNull() ?: 0,
        )
    }

    fun isNotFillData(): Boolean{
        return listOf(
            checkMandatoryField(binding.tfName),
            checkMandatoryField(binding.tfCoordinate),
            checkMandatoryField(binding.tfPlannedVisit),
            checkMandatoryField(binding.tfPeriod),
            checkMandatoryField(binding.tfPrice)
        ).any{ it.not() }
    }

    fun isNotValidateData(): Boolean{
        return listOf(
            checkValidateField(MyConst.BAD_COORDINATE) {
                val latitude = binding.tfCoordinate.editText?.text!!.split(",")[0].toDouble()
                val longitude = binding.tfCoordinate.editText?.text!!.split(",")[1].toDouble()
            },
            checkValidateField(MyConst.BAD_DATE) {
                val planned_month = binding.tfPlannedVisit.editText?.text.toString().split(",")[1].trim().toInt()
                val planned_year = binding.tfPlannedVisit.editText?.text.toString().split(",")[2].trim().toInt()
                if (planned_year !in 2000..2300 || planned_month !in 1..12) throw Exception()
            },
            checkValidateField(MyConst.BAD_PRICE) {
                val price = binding.tfPrice.editText?.text.toString().toInt()
                if(price < 0) throw Exception()
            },
            checkValidateField(MyConst.BAD_PERIOD) {
                val period = binding.tfPeriod.editText?.text.toString().toInt()
                if(period < 1) throw Exception()
            },
        ).any{ it.not() }
    }

    //проверяем заполненность обязательного поля и подсвечиваем ошибку если пусто
    fun checkMandatoryField(textInputLayout: TextInputLayout): Boolean{
        val errorText = MyConst.MANDATORY_FIELD
        if (textInputLayout.editText?.text.isNullOrBlank()) {
            textInputLayout.error = errorText
            textInputLayout.isErrorEnabled = true
            return false
        } else{
            textInputLayout.error = null
            textInputLayout.isErrorEnabled = false
        }
        return true
    }

    //проверяем не дропнет ли ошибку при валидации, обрабатываем ее
    fun checkValidateField(errorMessage: String, action: () -> Unit): Boolean{
        try {
            action()
            return true
        } catch (e: Exception){
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            Log.d("My.CreateVisit", "validate error: ${e.message}")
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.clients.collect { clients ->
                val customers = clients.map { it.name }.toTypedArray()
                withContext(Dispatchers.Main) {
                    (binding.tfName.editText as MaterialAutoCompleteTextView).setSimpleItems(
                        customers
                    )
                    clientList.clear()
                    clientList.addAll(clients)

                    val selectClient = clients.find { it.clietn_id == currentClient?.clietn_id }
                    selectClient?.let { client ->
                        val autoCompleteTextView = binding.tvName
                        autoCompleteTextView.setText(client.name, false)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}