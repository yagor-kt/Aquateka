package com.subefu.aquateka.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.subefu.aquateka.databinding.FragmentMapBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.adapter.VisitCardAdapter
import com.subefu.aquateka.viewmodel.MainViewModel
import com.subefu.aquateka.viewmodel.MainViewModelFactory
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.getValue

@RequiresApi(Build.VERSION_CODES.O)
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView

    private lateinit var rvAdapter: VisitCardAdapter
    var currantDay = LocalDate.now()
    var currentVisits = listOf<VisitWithClient>()
    var currentClients = listOf<Client>()

    var isViewMonthSelection = false

    private val sharedViewModel: MainViewModel by activityViewModels {
        val dataBase = DataBase.getDB(requireContext().applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        MainViewModelFactory(repository)
    }

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

        //orders
        setupRecyclerView()
        setupChips()

        sharedViewModel.visitsOnMap
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { visits ->
                currentVisits = visits
                updateVisits()
                Log.d("MyDB", visits.toString())
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        sharedViewModel.clients
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { clients ->
                currentClients = clients
                Log.d("MyDB", clients.toString())
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    fun setupRecyclerView(){
        rvAdapter = VisitCardAdapter(
            emptyList(),
            onItemClick = { item ->
                val bottomSheet = VisitInfoFragment.newInstance(
                    visit = item,
                )
                bottomSheet.show(childFragmentManager, "MyBottomSheetDialog")
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
    }

    fun setupChips(){
        binding.chipGroupMap.setOnCheckedStateChangeListener { group, checkedIds ->
            var points: List<Point> = emptyList()

            when(checkedIds.firstOrNull()){
                binding.chipAllClient.id -> {
                    isViewMonthSelection = false
                     points = currentClients.map {
                        Point(it.latitude.toDouble(), it.longitude.toDouble())
                     }
                    updateShortInfo(currentClients)
                    setPointsOnMap(points)
                    rvAdapter.updateList(emptyList())
                }
                binding.chipCurrentMonth.id -> {
                    isViewMonthSelection = false
                    sharedViewModel.loadVisitsOnMap(currantDay.monthValue, currantDay.year)
                    updateVisits()
                }
                binding.chipMonth.id -> {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    DatePickerDialog(requireContext(), 0, {_,selectedYear,selectedMonth,selectedDay ->
                        Log.d("MyMap", "$selectedYear,$selectedMonth,$selectedDay")
                        sharedViewModel.loadVisitsOnMap(selectedMonth+1, selectedYear)
                        binding.chipMonth.text = LocalDate.of(selectedYear, selectedMonth+1, 1).month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                        isViewMonthSelection = true
                    }, year, month, day).show()
                    updateVisits()
                }
            }
        }

        binding.chipMonth.setOnClickListener { view ->
            if (isViewMonthSelection){
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(requireContext(), 0, {_,selectedYear,selectedMonth,selectedDay ->
                    Log.d("MyMap", "$selectedYear,$selectedMonth,$selectedDay")
                    sharedViewModel.loadVisitsOnMap(selectedMonth+1, selectedYear)
                    binding.chipMonth.text = LocalDate.of(selectedYear, selectedMonth+1, 1).month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                }, year, month, day).show()
                updateVisits()
            }
        }
    }

    fun updateVisits(){
        rvAdapter.updateList(currentVisits)
        val points = currentVisits.map {
            Point(it.visit.latitude.toDouble(), it.visit.longitude.toDouble()
            )
        }
        setPointsOnMap(points)
        updateShortInfo(currentVisits)
    }

    fun updateShortInfo(visits: List<VisitWithClient>){
        val all = visits.size
        val active = visits.filter { it.visit.status == MyConst.PLANNED }.size

        binding.apply {
            binding.tvAll.text = "Всего: $all"
            binding.tvActive.text = "Активных: $active"
        }
    }
    fun updateShortInfo(visits: List<Client>): Boolean{
        val all = visits.size

        binding.apply {
            binding.tvAll.text = "Всего: $all"
            binding.tvActive.text = ""
        }
        return true
    }

    fun setPointsOnMap(points: List<Point>){
        mapView.run{
            map.mapObjects.clear()
            points.onEach {
                map.mapObjects.addPlacemark(it)
            }
            map.mapObjects.addTapListener { mapObject, point ->
                Toast.makeText(requireContext(), mapObject.userData.toString(), Toast.LENGTH_LONG).show()
                true
            }
            getBoundingBoxForPosition(points).let { boundingBox ->
                if(mapView.height != 0){
                    val topLeft = ScreenPoint(70f, 70f)
                    val width = binding.mapView.width.toFloat()
                    val height = binding.mapView.height.toFloat()
                    Log.d("MyMap", "$width, $height")
                    val bottomRight = ScreenPoint(width - 70f, height - 70f)
                    val screenRect = ScreenRect(topLeft, bottomRight)
                    mapWindow.focusRect = screenRect
                }
                val cameraPosition = map.cameraPosition(boundingBox, 0f, 0f, null)
                map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1.5f), null)
            }
        }
    }

    fun getBoundingBoxForPosition(points: List<Point>): BoundingBox{
        if(points.isEmpty()) return BoundingBox(Point(54.710162, 20.510137), Point(43.983211, 132.919176))

        var maxLat = points[0].latitude
        var minLat = points[0].latitude
        var maxLon = points[0].longitude
        var minLon = points[0].longitude

        for(point in points){
            if(point.latitude < minLat) minLat = point.latitude
            if(point.latitude > maxLat) maxLat = point.latitude
            if(point.longitude < minLon) minLon = point.longitude
            if(point.longitude > maxLon) maxLon = point.longitude
        }

        val southWest = Point(minLat, minLon)
        val northEast = Point(maxLat, maxLon)

        return BoundingBox(southWest, northEast)
    }

    override fun onResume() {
        super.onResume()
        binding.chipGroupMap.children
            .find { it.id == binding.chipCurrentMonth.id }
            ?.isSelected = true
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
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