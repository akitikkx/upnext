package com.theupnextapp.bindings

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import org.jsoup.Jsoup

@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url : String?) {
    Glide.with(imageView.context).load(url).into(imageView)
}

@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view : View, it : Any?) {
    view.visibility = if (it != null) View.GONE else View.VISIBLE
}

@BindingAdapter("fromHtml")
fun fromHtml(view : TextView, html : String?) {
    if (!html.isNullOrEmpty()) {
        view.text = Jsoup.parse(html).text()
    }
}

@BindingAdapter("showProgress")
fun showProgress(view : ProgressBar, show : Boolean) {
    if (show) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("loadingVisibility")
fun loadingVisibility(view : View, isLoading : Boolean) {
    if (isLoading) {
        view.alpha = .5f
    } else {
        view.alpha = 1f
    }
}

@BindingAdapter("canClickItem")
fun canClickItem(view : View, isLoading: Boolean) {
    view.isClickable = !isLoading
    view.isFocusable = !isLoading
}