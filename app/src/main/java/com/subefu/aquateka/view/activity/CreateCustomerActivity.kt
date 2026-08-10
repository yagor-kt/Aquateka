package com.subefu.aquateka.view.activity

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.subefu.aquateka.R
import com.subefu.aquateka.databinding.ActivityCreateCustomerBinding
import com.subefu.aquateka.databinding.ActivityCreateOrderBinding

class CreateCustomerActivity : AppCompatActivity() {

    private var _binding: ActivityCreateCustomerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityCreateCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        val type = intent.getStringExtra("type")
        binding.tvTitle.text =
            if(type == "create") "Создание клиента"
            else "Редактирвоание клиента"

        binding.btCancelled.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Отменить ${binding.tvTitle.text}?")
                .setMessage("Изменения не будут сохранены")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    this.finish()
                }
            builder.show()
        }

        binding.btPreserve.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Сохранить клиента?")
                .setNegativeButton("НЕТ"){dialog, witch ->
                    dialog.cancel()
                }
                .setPositiveButton("ДА"){dialog, witch ->
                    dialog.cancel()
                    //TODO(сохранить заказ в бд)
                    this.finish()
                }
            builder.show()
        }
    }
}