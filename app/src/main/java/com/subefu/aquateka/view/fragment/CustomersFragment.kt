package com.subefu.aquateka.view.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.size
import androidx.core.widget.doOnTextChanged
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.FragmentCustomersBinding
import com.subefu.aquateka.databinding.FragmentOrdersBinding
import com.subefu.aquateka.view.activity.CreateCustomerActivity
import com.subefu.aquateka.view.activity.CreateOrderActivity
import com.subefu.aquateka.view.activity.ProfileCustomerActivity
import com.subefu.aquateka.view.adapter.CustomersCardAdapter
import com.subefu.aquateka.view.adapter.OrdersCardAdapter

class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvAdapter: CustomersCardAdapter
    val list = listOf("Alex", "Bruno", "Flew", "Uvritulatuhalse")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAdapter = CustomersCardAdapter(list, onItemClick = {
                item ->
            val intent = Intent(requireContext(), ProfileCustomerActivity::class.java)
            //todo(передать заказчика реализовав parcelable)
            intent.putExtra("customer", item)
            startActivity(intent)
        })

        binding.rvCustomers.apply{
            adapter = rvAdapter
            setHasFixedSize(true)
            Log.d("MyLog", this.size.toString())
        }

        binding.fabAddCustomer.setOnClickListener {
            val intent = Intent(requireContext(), CreateCustomerActivity::class.java)
            intent.putExtra("type", "create")
            startActivity(intent)
        }

        binding.searchLayout.editText?.doOnTextChanged {text, _, _, _ ->
            //todo(сделать фильтрацию по нескольким полям)
            val newList = list.filter { it.contains(text.toString()) }
            rvAdapter.updateList(newList)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}