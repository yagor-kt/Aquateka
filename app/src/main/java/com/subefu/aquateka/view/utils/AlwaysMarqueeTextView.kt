package com.subefu.aquateka.view.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class AlwaysMarqueeTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    // База считает, что этот элемент ВСЕГДА в фокусе
    override fun isFocused(): Boolean = true
}