package com.subefu.aquateka.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.FragmentOrderInfoBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.activity.CreateVisitActivity
import com.subefu.aquateka.view.activity.ProfileClientActivity
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.getValue

@RequiresApi(Build.VERSION_CODES.O)
class VisitInfoFragment : BottomSheetDialogFragment() {

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
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private val viewModel: EditItemViewModel by viewModels{
        val dataBase = DataBase.getDB(requireContext().applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        EditItemViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val visitWithClient = visitWithClient ?: throw Exception("null visit")
        val visit = visitWithClient.visit
        val client = visitWithClient.client

        val customerInfo = "ФИО: ${client.name}\nТелефон: ${client.phone}\nКоординаты: ${visit.latitude}/${visit.longitude}\nАдрес: ${visit.address ?: "нет"}"
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
                MyConst.COMPLETED -> ContextCompat.getColorStateList(requireContext(), R.color.green)
                MyConst.POSTPONED -> ContextCompat.getColorStateList(requireContext(), R.color.blue)
                MyConst.PLANNED -> ContextCompat.getColorStateList(requireContext(), R.color.orange)
                else -> ContextCompat.getColorStateList(requireContext(), R.color.dark_surface)
            }

            tvCustomerInfo.setOnClickListener {
                val intent = Intent(requireContext(), ProfileClientActivity::class.java).apply {
                    putExtra(MyConst.CLIENT, client)
                }
                startActivity(intent)
            }
        }

        binding.btDelete.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Удалить визит ${client.name}?")
                .setMessage("ВЫ хотите удалить визит ${client.name} по координатам ${visit.latitude}/${visit.longitude}?\nОтменить это действие невозможно!")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    viewModel.deleteVisit(visit)
                    this@VisitInfoFragment.dismiss()
                }
            builder.show()
        }

        binding.btEdit.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Изменить визит \"${client.name}?\"")
                .setMessage("ВЫ хотите изменить визит \"${client.name}\" по координатам ${visit.latitude}/${visit.longitude}?")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    val intent = Intent(requireContext(), CreateVisitActivity::class.java).apply {
                        putExtra(MyConst.VISIT_WITH_CLIENT, visitWithClient)
                    }
                    startActivity(intent)
                    this@VisitInfoFragment.dismiss()
                }
            builder.show()
        }
        Log.d("MyOrderInfo-open", visitWithClient.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_VISIT_ITEM = "arg_visit_item"

        fun newInstance(visit: VisitWithClient): VisitInfoFragment {
            return VisitInfoFragment().apply {
                arguments = bundleOf(
                    ARG_VISIT_ITEM to visit
                )
            }
        }
    }
}