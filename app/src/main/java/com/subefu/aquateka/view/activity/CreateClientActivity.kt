package com.subefu.aquateka.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputLayout
import com.subefu.aquateka.App
import com.subefu.aquateka.databinding.ActivityCreateCustomerBinding
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import kotlin.getValue

class CreateClientActivity : AppCompatActivity() {

    private var _binding: ActivityCreateCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditItemViewModel by viewModels{
        EditItemViewModelFactory(App.repository)
    }

    private var currentClient: Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityCreateCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.root.id)) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, imeInsets.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        val type = intent.getStringExtra(MyConst.TYPE)

        if(type == MyConst.CREATE)
            binding.tvTitle.text = "Создание клиента"
        else{
            binding.tvTitle.text = "Редактирвоание клиента"
            setupEditData()
        }

        binding.btCancelled.setOnClickListener {
            val builder = getCancelledClientDialog()
            builder.show()
        }

        binding.btPreserve.setOnClickListener {
            if(isNotFillData() || isNotValidateData()) return@setOnClickListener

            val builder = getPreserveClientDialog()
            builder.show()
        }
    }

    fun setupEditData(){
        val client = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(MyConst.CLIENT, Client::class.java)
         else
            intent.getParcelableExtra(MyConst.CLIENT)

        client?.let { client ->
            currentClient = client
            fillUiClientData(client)
        }
    }

    fun fillUiClientData(client: Client){
        binding.apply {
            tfAddress.editText?.setText(client.address)
            tfComment.editText?.setText(client.comment)
            tfCoordinate.editText?.setText("${client.latitude},${client.longitude}")
            tfName.editText?.setText(client.name)
            tfPeriod.editText?.setText(client.period_month.toString())
            tfPhone.editText?.setText(client.phone)
        }
    }

    fun getCancelledClientDialog(): AlertDialog.Builder{
        return AlertDialog.Builder(this)
            .setTitle("Отменить ${binding.tvTitle.text}?")
            .setMessage("Изменения не будут сохранены")
            .setNegativeButton("НЕТ"){dialog, witch ->
                dialog.cancel()
            }
            .setPositiveButton("ДА"){dialog, witch ->
                dialog.cancel()
                this.finish()
            }
    }

    fun getPreserveClientDialog(): AlertDialog.Builder{
        val builder = AlertDialog.Builder(this)
        val name = binding.tfName.editText?.text.toString().trim()

        builder.setTitle("Сохранить клиента \"$name\"?")
            .setNegativeButton("НЕТ"){dialog, witch -> dialog.cancel() }
            .setPositiveButton("ДА"){dialog, witch ->
                val client = getClientFromUI()
                viewModel.saveClient(currentClient, client.copy())

                dialog.cancel()
                this.finish()
            }
        return builder
    }

    fun getClientFromUI(): Client{
        return Client(
            clietn_id = currentClient?.clietn_id ?: 0,
            name = binding.tfName.editText?.text.toString().trim(),
            phone = binding.tfPhone.editText?.text.toString().trim(),
            address = binding.tfAddress.editText?.text.toString().trim(),
            latitude = binding.tfCoordinate.editText?.text.toString().split(",").first().trim().toDouble(),
            longitude = binding.tfCoordinate.editText?.text.toString().split(",").last().trim().toDouble(),
            period_month = binding.tfPeriod.editText?.text.toString().trim().toIntOrNull() ?: 1,
            comment = binding.tfComment.editText?.text.toString().trim(),
        )
    }

    fun isNotFillData(): Boolean{
        return listOf(
            checkMandatoryField(binding.tfName),
            checkMandatoryField(binding.tfPhone),
            checkMandatoryField(binding.tfCoordinate),
        ).any{ it.not() }
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}