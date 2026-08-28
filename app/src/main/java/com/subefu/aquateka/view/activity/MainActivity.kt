package com.subefu.aquateka.view.activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.ActivityMainBinding
import com.subefu.aquateka.model.data.db.DataBase
import com.subefu.aquateka.model.data.db.utill.AppMessage
import com.subefu.aquateka.model.data.repository.AppEventBus
import com.subefu.aquateka.model.data.repository.RepositoryImpl
import com.subefu.aquateka.viewmodel.MainViewModel
import com.subefu.aquateka.viewmodel.MainViewModelFactory
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(this.getColor(R.color.dark_surface)))
        setContentView(binding.root)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.botNav.apply {
            val navHostFragment = supportFragmentManager
                .findFragmentById(binding.navHostFragment.id) as NavHostFragment
            val navController = navHostFragment.navController
            setupWithNavController(navController)
        }

        MapKitFactory.initialize(this)

        AppEventBus.events
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { message ->
                when(message){
                    is AppMessage.Error -> showSnackBar(message.text, true)
                    is AppMessage.Success -> showSnackBar(message.text, false)
                    else -> {}
                }
                Log.d("MyMain", "event: $message")
            }
            .launchIn(lifecycleScope)
    }

    fun showSnackBar(text: String, isError: Boolean){
        val view = binding.root
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).apply {
            if (isError)
                setBackgroundTint(Color.RED)
            setAnchorView(binding.botNav)
        }.show()
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