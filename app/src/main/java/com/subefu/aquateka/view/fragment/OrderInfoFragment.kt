package com.subefu.aquateka.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.FragmentOrderInfoBinding
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.activity.ProfileCustomerActivity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class OrderInfoFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentOrderInfoBinding? = null
    private val binding get() = _binding!!

    private val visitWithClient: VisitWithClient? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_VISIT_ITEM, VisitWithClient::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_VISIT_ITEM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        val visitWithClient = visitWithClient ?: throw Exception("null visit")
        val visit = visitWithClient.visit
        val client = visitWithClient.client

        val customerInfo = "ФИО: ${client.name}\nТелефон: ${client.phone}\nКоординаты: ${client.latitude}/${client.longitude}\nАдрес: ${client.address ?: "нет"}"
        val visitInfo = "Цена: ${visit.price}\nКоментарий: ${visit.comment}\nЗапчасти: ${visit.parts}"
        val nextVisit = LocalDate.of(visit.planned_year, visit.planned_month, 1)
            .plusMonths(visit.period.toLong())
            .format(formatter)

        val periodInfo = "Периодичность: ${visit.period}\nСледующий визит: ${nextVisit}"
        val actualDate = if(visit.actual_date != 0){
            Instant.ofEpochMilli(visit.actual_date.toLong())
                .atZone(ZoneId.systemDefault())
                .toLocalDate().format(formatter)
        }
        else "---"

        binding.apply {
            tvCustomerInfo.text = customerInfo
            tvOrderInfo.text = visitInfo
            tvPeriodInfo.text = periodInfo
            tvStatus.text = visit.status
            tvDate.text = actualDate
            ivStatusColor.imageTintList = when(visit.status){
                "COMPLETED" -> ContextCompat.getColorStateList(requireContext(), R.color.green)
                "POSTPONED" -> ContextCompat.getColorStateList(requireContext(), R.color.blue)
                "PLANNED" -> ContextCompat.getColorStateList(requireContext(), R.color.orange)
                else -> ContextCompat.getColorStateList(requireContext(), R.color.dark_surface)
            }

            tvCustomerInfo.setOnClickListener {
                val intent = Intent(requireContext(), ProfileCustomerActivity::class.java).apply {
                    putExtra("client", client)
                }
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_VISIT_ITEM = "arg_visit_item"

        fun newInstance(visit: VisitWithClient): OrderInfoFragment {
            return OrderInfoFragment().apply {
                arguments = bundleOf(
                    ARG_VISIT_ITEM to visit
                )
            }
        }
    }
}