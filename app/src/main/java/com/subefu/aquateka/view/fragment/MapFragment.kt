package com.subefu.aquateka.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.subefu.aquateka.databinding.FragmentMapBinding
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.adapter.OrdersCardAdapter
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView

    private lateinit var rvAdapter: OrdersCardAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        MapKitFactory.initialize(context)
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater)
        mapView = binding.mapView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAdapter = OrdersCardAdapter(emptyList(), onItemClick = {
                item ->
//            val bottomSheet = OrderInfoFragment.newInstance(
//                VisitWithClient()
//            )
//            bottomSheet.show(childFragmentManager, "MyBottomSheetDialog")
        }, onLongItemClick = {
                item ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Завершить заказ?")
                .setNegativeButton("Перенести"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    //TODO(завершение заказа, след дата = текущая + период)
                }
                .setNeutralButton("Выбрать месяц", {dialog, witch ->
                    dialog.cancel()
                })
            builder.show()
        })

        binding.rvOrders.apply {
            adapter = rvAdapter
            setHasFixedSize(true)
        }

        binding.chipGroupMap.setOnCheckedStateChangeListener { group, checkedIds ->
            //TODO("настроить фильтарцию списка")
            when(checkedIds.firstOrNull()){
                binding.chipAllClient.id -> {}
                binding.chipCurrentMonth.id -> {}
                binding.chipMonth.id -> {}
            }
            updateShortInfo("9", "9")
        }

    }

    fun updateShortInfo(all: String, active: String){
        binding.tvAll.text = "Всего: $all"
        binding.tvActive.text = "Активных:$active"
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()

        mapView.run {
            val target = Point(54.171970, 45.134150)

            map.move(CameraPosition(target, 14.0f, 0.0f, 0.0f))
            map.mapObjects.addPlacemark(Point(54.171973, 45.134155))
        }
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}