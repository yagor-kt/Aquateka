package com.subefu.aquateka.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.subefu.aquateka.databinding.ActivityCreateCustomerBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import kotlin.getValue

class CreateClientActivity : AppCompatActivity() {

    private var _binding: ActivityCreateCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditItemViewModel by viewModels{
        val dataBase = DataBase.getDB(applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        EditItemViewModelFactory(repository)
    }

    private var currentClient: Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityCreateCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        val type = intent.getStringExtra("type")

        if(type == MyConst.CLIENT)
            binding.tvTitle.text = "Создание клиента"
        else{
            binding.tvTitle.text = "Редактирвоание клиента"
            setupEditData()
        }

        binding.btCancelled.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Отменить ${binding.tvTitle.text}?")
                .setMessage("Изменения не будут сохранены")
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
            if(isNotFillData() || isNotValidateData()) return@setOnClickListener

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Сохранить клиента?")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()

                    val client = Client(
                        clietn_id = currentClient?.clietn_id ?: 0,
                        name = binding.tfName.editText?.text.toString().trim(),
                        phone = binding.tfPhone.editText?.text.toString().trim(),
                        address = binding.tfAddress.editText?.text.toString().trim(),
                        latitude = binding.tfCoordinate.editText?.text.toString().split(",").first().trim().toDouble(),
                        longitude = binding.tfCoordinate.editText?.text.toString().split(",").last().trim().toDouble(),
                        period_month = binding.tfPeriod.editText?.text.toString().trim().toIntOrNull() ?: 1,
                        comment = binding.tfComment.editText?.text.toString().trim(),
                    )

                    //если пустой адрес, то ищем по координатам
                    //иначе если "-", то запишем пустой адрес
                    //иначе пишем в адрес то что есть
                    if(currentClient != null)
                        if (client.address.isNullOrBlank())
                            viewModel.updateClient(client, true)
                        else if(client.address == "-")
                            viewModel.updateClient(client.copy(address = ""))
                        else
                            viewModel.updateClient(client)
                    else
                        viewModel.insertClient(client)

                    setResult(RESULT_OK, Intent().putExtra(MyConst.CLIENT, client))
                    this.finish()
                }
            builder.show()
        }
    }

    fun setupEditData(){
        val client = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(MyConst.CLIENT,
                Client::class.java)
        } else {
            intent.getParcelableExtra(MyConst.CLIENT)
        }

        client?.let { client ->
            currentClient = client

            binding.apply {
                tfAddress.editText?.setText(client.address)
                tfComment.editText?.setText(client.comment)
                tfCoordinate.editText?.setText("${client.latitude},${client.longitude}")
                tfName.editText?.setText(client.name)
                tfPeriod.editText?.setText(client.period_month.toString())
                tfPhone.editText?.setText(client.phone)
            }
        }
    }

    fun isNotFillData(): Boolean{
        return listOf(
            checkMandatoryField(binding.tfName),
            checkMandatoryField(binding.tfPhone),
            checkMandatoryField(binding.tfCoordinate),
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

    fun isNotValidateData(): Boolean{
        return listOf(
            checkValidateField(MyConst.BAD_COORDINATE) {
                val latitude = binding.tfCoordinate.editText?.text!!.split(",")[0].toDouble()
                val longitude = binding.tfCoordinate.editText?.text!!.split(",")[1].toDouble()
            },
            checkValidateField(MyConst.BAD_PHONE) {
                val phone = binding.tfPhone.editText?.text.toString()
                if(phone.length != 11 || phone.toLongOrNull() == null) throw Exception()
            }
        ).any{ it.not() }
    }

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}