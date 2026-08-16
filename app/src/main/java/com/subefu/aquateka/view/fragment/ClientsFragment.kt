package com.subefu.aquateka.view.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.size
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.subefu.aquateka.databinding.FragmentCustomersBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.activity.CreateCustomerActivity
import com.subefu.aquateka.view.activity.ProfileCustomerActivity
import com.subefu.aquateka.view.adapter.CustomersCardAdapter
import com.subefu.aquateka.viewmodel.MainViewModel
import com.subefu.aquateka.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.getValue

@RequiresApi(Build.VERSION_CODES.O)
class ClientsFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: MainViewModel by activityViewModels {
        val dataBase = DataBase.getDB(requireContext().applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        MainViewModelFactory(repository)
    }

    private lateinit var rvAdapter: CustomersCardAdapter
    var clients = listOf<Client>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.clients
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { clients ->
                this.clients = clients
                rvAdapter.updateList(clients)
                updateShortInfo(clients)
                Log.d("MyDB", clients.toString())
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        rvAdapter = CustomersCardAdapter(
            clients = clients,
            onItemClick = { item ->
                val intent = Intent(requireContext(), ProfileCustomerActivity::class.java)
                intent.putExtra(MyConst.CUSTOMER, item)
                startActivity(intent)
        })

        binding.rvCustomers.apply{
            adapter = rvAdapter
            Log.d("MyLog", this.size.toString())
        }

        binding.fabAddCustomer.setOnClickListener {
            val intent = Intent(requireContext(), CreateCustomerActivity::class.java)
            intent.putExtra(MyConst.TYPE, MyConst.CREATE)
            startActivity(intent)
        }

        binding.searchLayout.editText?.doOnTextChanged {text, _, _, _ ->
            //todo(сделать фильтрацию по нескольким полям)
//            val newList = list.filter { it.contains(text.toString()) }
            //rvAdapter.updateList(newList)
        }

        binding.imExport.setOnClickListener {
            Toast.makeText(requireContext(), "Экспорт в разработке", Toast.LENGTH_SHORT).show()
            //TODO(Сделать экспорт заказов по текущему месяцу)
        }
        binding.imImport.setOnClickListener {
            Toast.makeText(requireContext(), "Импорт в разработке", Toast.LENGTH_SHORT).show()
            //TODO(Сделать импорт заказов по текущему месяцу)
        }
    }

    fun updateShortInfo(visits: List<Client>){
        val all = visits.size

        binding.apply {
            tvAll.text = "Всего: $all"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}