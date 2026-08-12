package com.subefu.aquateka.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.ActivityCreateOrderBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.getValue

class CreateOrderActivity : AppCompatActivity() {

    private var _binding: ActivityCreateOrderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditItemViewModel by viewModels{
        val dataBase = DataBase.getDB(applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        EditItemViewModelFactory(repository)
    }

    private val clientList = mutableListOf<Client>()
    private var currentClient: Client? = null

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
        val type = intent.getStringExtra("type")
        binding.tvTitle.text =
            if(type == "create") "Создание заказа"
            else "Редактирвоание заказа"

        binding.tfName.editText?.doAfterTextChanged { view ->
            currentClient = clientList.find { it.name == binding.tfName.editText?.text.toString()}
            Log.d("MyCOA", currentClient?.name.toString())
            currentClient?.let {
                binding.apply {
                    tfAddress.editText?.setText(it.address ?: "")
                    tfCoordinate.editText?.setText("${it.latitude},${it.longitude}")
                }
            }
        }

        binding.btCancelled.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Отменить ${binding.tvTitle.text}?")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    this.finish()
                }
            builder.show()
        }

        binding.btPreserve.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Сохранить заказ?")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    /*val client = Client(
                        cllietn_id = 0,
                        name = binding.tfName.editText?.text.toString(),
                        phone = binding.tfPhone.editText?.text.toString(),
                        address = binding.tfAddress.editText?.text.toString(),
                        latitude = binding.tfCoord.editText?.text?.split(",")?.first()?.toFloat() ?: 0f,
                        longitude = binding.tfCoord.editText?.text?.split(",")?.last()?.toFloat() ?: 0f,
                        period_month = 0,
                        comment = binding.tfComment.editText?.text.toString(),
                        create_at = Date().time.toInt()
                    )*/
                    val visit = Visit(
                        id = 0,
                        clientId = currentClient?.cllietn_id ?: throw NullPointerException("Введите клиента"),
                        address = binding.tfAddress.editText?.text.toString(),
                        latitude = binding.tfCoordinate.editText?.text.toString().split(",").first().trim().toFloat(),
                        longitude = binding.tfCoordinate.editText?.text.toString().split(",").last().trim().toFloat(),
                        planned_month = binding.tfPlannedVisit.editText?.text.toString().split(",")[1].trim().toInt(),
                        planned_year = binding.tfPlannedVisit.editText?.text.toString().split(",")[2].trim().toInt(),
                        actual_date = 0,
                        status = MyConst.PLANNED,
                        work_type = binding.tfWorkType.editText?.text.toString(),
                        price = binding.tfPrice.editText?.text.toString().toInt(),
                        parts = binding.tfDetails.editText?.text.toString(),
                        comment = binding.tfComment.editText?.text.toString(),
                        period = binding.tfPeriod.editText?.text.toString().toInt(),
                    )

                    viewModel.insertVisit(visit)
                    this.finish()
                }
            builder.show()
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
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}