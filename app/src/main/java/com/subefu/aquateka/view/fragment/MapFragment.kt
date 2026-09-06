package com.subefu.aquateka.view.fragment

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.FragmentMapBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.model.domain.MyConst
import com.subefu.aquateka.model.domain.model.Client
import com.subefu.aquateka.model.domain.model.VisitWithClient
import com.subefu.aquateka.view.adapter.VisitCardAdapter
import com.subefu.aquateka.view.utils.TopBottomPaddingDecoration
import com.subefu.aquateka.view.utils.VisitCardAdapterFactory
import com.subefu.aquateka.viewmodel.MainViewModel
import com.subefu.aquateka.viewmodel.MainViewModelFactory
import com.yandex.mapkit.Animation
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.getValue

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels {
        val dataBase = DataBase.getDB(requireContext().applicationContext)
        val repository = RepositoryImpl(dataBase.getDao())
        MainViewModelFactory(repository)
    }

    private lateinit var mapView: MapView
    private lateinit var markerTapListener : MapObjectTapListener
    private lateinit var rvAdapter: VisitCardAdapter

    private var currantDay = LocalDate.now()
    private var currentVisits = listOf<VisitWithClient>()
    private var currentClients = listOf<Client>()

    private var isViewMonthSelection = false
    private var points: List<Pair<Point, Any>> = emptyList()
    var isChangeMap = false

    var currentPointGroup = "visits"

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

        setupRecyclerView()
        setupChips()

        sharedViewModel.visitsOnMap
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { visits ->
                Log.d("MyMap", "on each visits")
                chooseCurrentMonthVisit(visits)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        sharedViewModel.clients
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { clients ->
                Log.d("MyMap", "on each clients")
                chooseAllClient(clients)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        markerTapListener = MapObjectTapListener { mapObject, point ->
            val placeMark = mapObject as? PlacemarkMapObject
            val client = placeMark?.userData as? Client
            val visitWithClient = placeMark?.userData as? VisitWithClient

            client?.let {
                AppEventBus.post(AppMessage.TouchMapPoint("Выбран клиент: ${client.name}", client))
            }
            visitWithClient?.let {
                AppEventBus.post(AppMessage.TouchMapPoint("Выбран визит: ${it.client.name}", visitWithClient))
            }

            true
        }
    }

    fun chooseCurrentMonthVisit(visits: List<VisitWithClient>){
        currentVisits = visits
        if (currentPointGroup != "visits") return
         updateVisits()
    }

    fun chooseAllClient(clients: List<Client>){
        currentClients = clients
        if (currentPointGroup != "clients") return
        updateClients()
    }

    fun updateClients(){
        points = currentClients.map { client ->
            Pair(
                Point(client.latitude, client.longitude),
                client
            )
        }
        Log.d("MyMap", "points on update clients: $points")
        updateShortInfo(currentClients)
        setPointsOnMap(points)
        rvAdapter.updateList(emptyList())
    }

    fun updateVisits(){
        points = currentVisits.map { visitWithClient ->
            Pair(
                Point(visitWithClient.visit.latitude, visitWithClient.visit.longitude),
                visitWithClient
            )
        }
        Log.d("MyMap", "points on update visits: $points")
        updateShortInfo(currentVisits)
        setPointsOnMap(points)
        rvAdapter.updateList(currentVisits)
    }

    fun setupChips(){
        binding.chipGroupMap.setOnCheckedStateChangeListener { group, checkedIds ->
            when(checkedIds.firstOrNull()){
                binding.chipAllClient.id -> {
                    currentPointGroup = "clients"
                    selectedAllClients()
                }
                binding.chipCurrentMonth.id -> {
                    currentPointGroup = "visits"
                    selectedChipCurrentMonth()
                }
                binding.chipMonth.id -> {
                    currentPointGroup = "visits"
                    selectedMonth()
                }
            }
        }

        binding.chipMonth.setOnClickListener { view ->
            if (isViewMonthSelection){
                selectedMonth()
            }
        }
    }

    fun selectedChipCurrentMonth(){
        isViewMonthSelection = false
        sharedViewModel.setCurrentDateForMap(currantDay.monthValue, currantDay.year)
        updateVisits()
    }

    fun selectedAllClients(){
        isViewMonthSelection = false
        updateClients()
    }

    fun selectedMonth(){
        val calendar = Calendar.getInstance()

        DatePickerDialog(requireContext(), 0, {_,selectedYear,selectedMonth,selectedDay ->
            sharedViewModel.setCurrentDateForMap(selectedMonth+1, selectedYear)
            binding.chipMonth.text = LocalDate.of(selectedYear, selectedMonth+1, 1)
                .month
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
            isViewMonthSelection = true
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun setPointsOnMap(items: List<Pair<Point, Any>>){
        points = items
        isChangeMap = false
        Log.d("MyMap", "points set on map: $points")
        mapView.map.mapObjects.clear()
        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_location_small)
        items.onEach { item ->
            mapView.map.mapObjects.addPlacemark(item.first, imageProvider).apply {
                addTapListener(markerTapListener)
                geometry = item.first
                userData = item.second
            }
        }
        if(mapView.height != 0)
            mapView.mapWindow.focusRect = getScreenRect()
        val boundingBox = getBoundingBoxForPosition(points.map { it.first })
        val cameraPosition = mapView.map.cameraPosition(boundingBox, 0f, 0f, null)
        mapView.map.move(cameraPosition, Animation(Animation.Type.SMOOTH, 1.2f), null)
    }

    fun setupRecyclerView(){
        rvAdapter = VisitCardAdapterFactory.getInstance(
            emptyList(),
            requireContext(),
            childFragmentManager
        ){ visitWithClient, mode ->
            if (mode == MyConst.APPROVE) {
                sharedViewModel.postponeVisit(null, mode, visitWithClient.visit)
                return@getInstance
            }
            else if (mode == MyConst.MANUAL_POSTPONE) {
                selectPostponeMonth{ date ->
                    sharedViewModel.postponeVisit(
                        if (mode == MyConst.MANUAL_POSTPONE)
                            date
                        else null,
                        mode,
                        visitWithClient.visit,
                    )
                }
            }
        }

        binding.rvOrders.apply {
            adapter = rvAdapter
            addItemDecoration(TopBottomPaddingDecoration(10, 100))
        }

    }

    fun selectPostponeMonth(dateSetListener: (Pair<Int, Int>) -> Unit){
        val calendar = Calendar.getInstance()

        DatePickerDialog(requireContext(), 0, {_,selectedYear,selectedMonth,selectedDay ->
            val date = Pair(selectedMonth + 1, selectedYear)
            dateSetListener(date)
        },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun updateShortInfo(visits: List<VisitWithClient>){
        val all = visits.size
        val active = visits.filter { it.visit.status == MyConst.PLANNED }.size

        binding.apply {
            binding.tvAll.text = "Всего: $all"
            binding.tvActive.text = "Активных: $active"
        }
    }
    fun updateShortInfo(clients: List<Client>): Boolean{
        val all = clients.size

        binding.apply {
            binding.tvAll.text = "Всего: $all"
            binding.tvActive.text = ""
        }
        return true
    }

    fun getScreenRect(): ScreenRect{
        val topLeft = ScreenPoint(70f, 70f)
        val width = binding.mapView.width.toFloat()
        val height = binding.mapView.height.toFloat()
        val bottomRight = ScreenPoint(width - 70f, height - 70f)
        return ScreenRect(topLeft, bottomRight)
    }

    fun getBoundingBoxForPosition(points: List<Point>): BoundingBox{
        if(points.isEmpty())
            //крайние точки России
            return BoundingBox(Point(54.710162, 20.510137), Point(43.983211, 132.919176))

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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        Log.d("MyMap", "points on start: $points")
        updateVisits()
        binding.chipGroupMap.check(binding.chipCurrentMonth.id)
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}