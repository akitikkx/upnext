package com.theupnextapp.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.R

@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    val requestOptions = RequestOptions()
        .placeholder(R.color.grey_light)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .error(R.color.grey_light)
        .fallback(R.color.grey_light)

    try {
        Glide.with(imageView.context)
            .load(url)
            .apply(requestOptions)
            .into(imageView)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}