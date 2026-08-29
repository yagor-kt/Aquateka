package com.subefu.aquateka.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.subefu.aquateka.databinding.ActivityProfileCustomerBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.adapter.VisitCardAdapter
import com.subefu.aquateka.view.utils.VisitCardAdapterFactory
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlin.getValue

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class ProfileClientActivity : AppCompatActivity() {

    private var _binding: ActivityProfileCustomerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditItemViewModel by viewModels{
        val dataBase = DataBase.getDB(applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        EditItemViewModelFactory(repository)
    }

    private var messageSubscriptionJob: Job? = null

    private lateinit var rvAdapter: VisitCardAdapter
    private lateinit var currentClient: Client
    private var visits = listOf<VisitWithClient>()

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

        val clientId = intent.getIntExtra(MyConst.CLIENT_ID, -1)
        viewModel.setClientId(clientId)

        viewModel.client
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .filterNotNull()
            .onEach { client ->
                currentClient = client
                updateUserInfo()
            }
            .launchIn(lifecycleScope)

        viewModel.visits
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { visits ->
                val newVisits = visits.map { it -> VisitWithClient(it, currentClient) }
                this.visits = newVisits
                rvAdapter.updateList(newVisits)
            }
            .launchIn(lifecycleScope)

        messageSubscriptionJob = AppEventBus.events
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { message ->
                when(message){
                    is AppMessage.Error -> showSnackBar(message.text, true)
                    is AppMessage.Success -> showSnackBar(message.text, false)
                    else -> {}
                }
                Log.d("MyProfile", "event: $message")
            }
            .launchIn(lifecycleScope)

        setupRV()

        binding.btDelete.setOnClickListener {
            val builder = getDeleteClientDialog()
            builder.show()
        }

        binding.btEdit.setOnClickListener {
            val builder = getEditClientDialog()
            builder.show()
        }

    }

    fun showSnackBar(text: String, isError: Boolean){
        val view = binding.root
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).apply {
            if (isError)
                setBackgroundTint(Color.RED)
        }.show()
    }

    fun updateUserInfo(){
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


    fun setupRV(){
        Log.d("MyProfileClient", "visits size: ${visits.size}")
        rvAdapter = VisitCardAdapterFactory.getInstance(
            visits,
            this,
            supportFragmentManager
        )

        binding.rvVisits.apply{
            adapter = rvAdapter
        }
    }

    fun getDeleteClientDialog(): AlertDialog.Builder{
        return AlertDialog.Builder(this)
            .setTitle("Удалить клиента \"${currentClient.name}?\"")
            .setMessage("Это действие невозможно отменить, также будут удалены все визиты этого клиента!")
            .setNegativeButton("НЕТ") { dialog, witch ->
                dialog.cancel()
            }
            .setPositiveButton("ДА") { dialog, witch ->
                dialog.cancel()
                this.finish()
                messageSubscriptionJob?.cancel()
                viewModel.deleteClient(currentClient)
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
                startActivity(intent)
            }
    }
}