package com.subefu.aquateka.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.subefu.aquateka.databinding.ActivityProfileCustomerBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import kotlin.getValue

class ProfileClientActivity : AppCompatActivity() {

    private var _binding: ActivityProfileCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditItemViewModel by viewModels{
        val dataBase = DataBase.getDB(applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        EditItemViewModelFactory(repository)
    }

    private lateinit var currentClient: Client

    private val startChildActivityLauncher
    = registerForActivityResult (ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val returnedValue = result.data?.getParcelableExtra<Client>(MyConst.CLIENT, Client::class.java)
            returnedValue?.let {
                currentClient = it
                loadUserInfo()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityProfileCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        currentClient = intent.getParcelableExtra(MyConst.CLIENT, Client::class.java)
            ?: throw NullPointerException("client is null, intent was incorrect")

        loadUserInfo()

        binding.btDelete.setOnClickListener {
            val builder = getDeleteClientDialog()
            builder.show()

            binding.btEdit.setOnClickListener {
                val builder = getEditClientDialog()
                builder.show()
            }
        }
    }

    fun loadUserInfo(){
        val userInfo = """
            |ФИО: ${currentClient.name}
            |Телефон: ${currentClient.phone}
            |Адрес: ${currentClient.address ?: "не указан"}
            |Координаты: ${currentClient.latitude} / ${currentClient.longitude}
            |Коментарий: ${currentClient.comment}
            |Периодичность: ${currentClient.period_month ?: "не указана"}
        """.trimMargin()
        binding.userInfo.text = userInfo.toString()
    }

    fun getDeleteClientDialog(): AlertDialog.Builder{
        return AlertDialog.Builder(this)
            .setTitle("Удалить клиента \"${currentClient.name}?\"")
            .setMessage("Это действие невозможно отменить, также будут удалены все визиты этого клиента!")
            .setNegativeButton("НЕТ") { dialog, witch ->
                dialog.cancel()
            }
            .setPositiveButton("ДА") { dialog, witch ->
                viewModel.deleteClient(currentClient)
                dialog.cancel()
                this.finish()
            }
    }

    fun getEditClientDialog(): AlertDialog.Builder{
        return AlertDialog.Builder(this)
            .setTitle("Изменить клиента \"${currentClient.name}?\"")
            .setNegativeButton("НЕТ") { dialog, witch ->
                dialog.cancel()
            }
            .setPositiveButton("ДА") { dialog, witch ->
                dialog.cancel()
                val intent = Intent(this, CreateClientActivity::class.java)
                intent.putExtra(MyConst.CLIENT, currentClient)
                startChildActivityLauncher.launch(intent)
            }
    }
}