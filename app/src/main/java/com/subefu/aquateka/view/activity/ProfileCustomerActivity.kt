package com.subefu.aquateka.view.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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

class ProfileCustomerActivity : AppCompatActivity() {

    private var _binding: ActivityProfileCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditItemViewModel by viewModels{
        val dataBase = DataBase.getDB(applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        EditItemViewModelFactory(repository)
    }

    lateinit var currentClient: Client

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

        currentClient = intent.getParcelableExtra(MyConst.CUSTOMER, Client::class.java)
            ?: throw NullPointerException("client is null, intent was incorrect")

        loadUserInfo()
        setClickListeners()
    }

    fun loadUserInfo(){
        val userInfo = StringBuilder().apply {
            append("ФИО: ${currentClient.name}\n")
            append("Телефон: ${currentClient.phone}\n")
            append("Адрес: ${currentClient.address ?: "не указан"}\n")
            append("Координаты: ${currentClient.latitude}/${currentClient.longitude}\n")
            append("Коментарий: ${currentClient.comment}\n")
            append("Периодичность: ${currentClient.period_month ?: "не указана"}")
        }
        binding.userInfo.text = userInfo.toString()
    }

    fun setClickListeners(){
        binding.btDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Изменить клиента \"${currentClient.name}?\"")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    val intent = Intent(this, CreateClientActivity::class.java).apply {
                        putExtra(MyConst.CUSTOMER, currentClient)
                    }
                    startActivity(intent)
                }
            builder.show()
        }

        binding.btEdit.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Изменить клиента \"${currentClient.name}?\"")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    val intent = Intent(this, CreateClientActivity::class.java).apply {
                        putExtra(MyConst.CUSTOMER, currentClient)
                    }
                    startActivity(intent)
                }
            builder.show()
        }
    }

    fun getAlertDialogBuilder(
        title: String, message: String?,
        negativeTitle: String, positiveTitle: String,
        onNegativeClick: (DialogInterface) -> Unit,
        onPositiveClick: (DialogInterface) -> Unit,
        ): AlertDialog.Builder
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message ?: "")
            .setNegativeButton(negativeTitle){dialog, witch ->
                onNegativeClick(dialog)
            }
            .setPositiveButton(positiveTitle){dialog, witch ->
                onPositiveClick(dialog)
            }
        return builder
    }

}