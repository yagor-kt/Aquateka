package com.subefu.aquateka.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.subefu.aquateka.BuildConfig
import com.subefu.aquateka.R
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MapActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapKitFactory.setApiKey(BuildConfig.MAP_API_kEY)
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mapView = findViewById(R.id.map_view)

        val target = Point(54.171970, 45.134150)
        mapView.map.move(CameraPosition(target, 14.0f, 0.0f,0.0f))

//        val imageProvide = ImageProvider.fromResource(this, R.drawable.ic_nav_customer)
        val placemark = mapView.map.mapObjects.addPlacemark(Point(54.171973, 45.134155))
//        placemark.setIcon(imageProvide)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}