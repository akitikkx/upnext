package com.theupnextapp.bindings

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.theupnextapp.R

@BindingAdapter("dynamicText")
fun setDynamicText(textView: TextView, isAlreadyConnected : Boolean) {
    if (isAlreadyConnected) {
        textView.setText(R.string.trakt_button_text_is_connected)
    } else {
        textView.setText(R.string.trakt_button_text_not_connected)
    }
}