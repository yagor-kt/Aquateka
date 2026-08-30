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
import com.subefu.aquateka.App
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.FragmentOrderInfoBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.Visit
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.activity.CreateVisitActivity
import com.subefu.aquateka.view.activity.ProfileClientActivity
import com.subefu.aquateka.viewmodel.EditItemViewModel
import com.subefu.aquateka.viewmodel.EditItemViewModelFactory
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.getValue

@RequiresApi(Build.VERSION_CODES.O)
class VisitInfoFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentOrderInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditItemViewModel by viewModels{
        EditItemViewModelFactory(App.repository)
    }

    private val visitWithClient: VisitWithClient? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arguments?.getParcelable(ARG_VISIT_ITEM, VisitWithClient::class.java)
        else {
            arguments?.getParcelable(ARG_VISIT_ITEM)
        }
    }
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private lateinit var client: Client
    private lateinit var visit: Visit

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
        visit = visitWithClient.visit
        client = visitWithClient.client

        setupUI()

        binding.tvCustomerInfo.setOnClickListener {
            val intent = Intent(requireContext(), ProfileClientActivity::class.java)
            intent.putExtra(MyConst.CLIENT_ID, client.clietn_id)
            startActivity(intent)
        }

        binding.btDelete.setOnClickListener {
            val builder = getDeleteVisitDialog()
            builder.show()
        }

        binding.btEdit.setOnClickListener {
            val builder = getEditVisitDialog()
            builder.show()
        }
    }

    fun setupUI(){
        val nextVisit = getNextDate()

        val actualDate = if(visit.actual_date != 0){
            LocalDate.ofEpochDay(visit.actual_date.toLong()).format(formatter)
        }
        else "[не выполнен]"

        binding.apply {
            tvCustomerInfo.text = getClientInfo()
            tvOrderInfo.text = getVisitInfo()
            tvPeriodInfo.text = getPeriodInfo(nextVisit)
            tvStatus.text = visit.status
            tvDate.text = actualDate

            ivStatusColor.imageTintList = when(visit.status){
                MyConst.COMPLETED -> ContextCompat.getColorStateList(requireContext(), R.color.green)
                MyConst.POSTPONED -> ContextCompat.getColorStateList(requireContext(), R.color.blue)
                MyConst.RESCHEDULE_FROM_PAST -> ContextCompat.getColorStateList(requireContext(), R.color.magenta)
                MyConst.PLANNED -> ContextCompat.getColorStateList(requireContext(), R.color.orange)
                else -> ContextCompat.getColorStateList(requireContext(), R.color.dark_surface)
            }
        }
    }

    fun getClientInfo() =
        """
            |ФИО: ${client.name}
            |Телефон: ${client.phone}
            |Координаты: ${visit.latitude} / ${visit.longitude}
            |Адрес: ${visit.address ?: "нет"}
        """.trimMargin()

    fun getVisitInfo() =
        """
            |Цена: ${visit.price}
            |Коментарий: ${visit.comment}
            |Запчасти: ${visit.parts}
        """.trimMargin()

    fun getPeriodInfo(nextVisit: String) =
        """
            |Периодичность: ${visit.period}
            |Следующий визит: $nextVisit
        """.trimMargin()

    fun getNextDate() =
        LocalDate.of(visit.planned_year, visit.planned_month, 1)
            .plusMonths(visit.period.toLong())
            .format(formatter)

    fun getDeleteVisitDialog(): AlertDialog.Builder{
        return AlertDialog.Builder(requireContext())
            .setTitle("Удалить визит ${client.name}?")
            .setMessage("Вы хотите удалить визит ${client.name} по координатам ${visit.latitude}/${visit.longitude}?\nОтменить это действие невозможно!")
            .setNegativeButton("НЕТ"){dialog, witch ->
                dialog.cancel()
            }
            .setPositiveButton("ДА"){dialog, witch ->
                viewModel.deleteVisit(visit)
                dialog.cancel()
                this@VisitInfoFragment.dismiss()
            }
    }

    fun getEditVisitDialog(): AlertDialog.Builder{
        return AlertDialog.Builder(requireContext())
            .setTitle("Изменить визит \"${client.name}?\"")
            .setMessage("Вы хотите изменить визит \"${client.name}\" по координатам ${visit.latitude}/${visit.longitude}?")
            .setNegativeButton("НЕТ"){dialog, witch ->
                dialog.cancel()
            }
            .setPositiveButton("ДА"){dialog, witch ->
                val intent = Intent(requireContext(), CreateVisitActivity::class.java)
                intent.putExtra(MyConst.VISIT_WITH_CLIENT, visitWithClient)
                startActivity(intent)
                dialog.cancel()
                this@VisitInfoFragment.dismiss()
            }
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