package com.subefu.aquateka.view.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.FragmentOrderInfoBinding
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.activity.ProfileCustomerActivity
import java.time.LocalDate
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val visitWithClient = visitWithClient ?: throw Exception("null visit")
        val visit = visitWithClient.visit
        val client = visitWithClient.client

        val customerInfo = "${client.name}\n${client.phone}\n${client.latitude}/${client.longitude}\n${client.address}"
        val visitInfo = "${visit.price}\nКоментарий: ${visit.comment}\nЗапчасти: ${client.name}"
        val nextVisit = LocalDate.of(visit.planned_year, visit.planned_month, 1).run {
            plusMonths(visit.period.toLong())
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            this.format(formatter)
        }
        val periodInfo = "Периодичность: ${visit.period}\nСледующий визит: ${nextVisit}"

        binding.apply {
            tvCustomerInfo.text = customerInfo
            tvOrderInfo.text = visitInfo
            tvPeriodInfo.text = periodInfo

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