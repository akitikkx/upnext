package com.theupnextapp.bindings

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theupnextapp.R
import com.theupnextapp.domain.ShowDetailArg

@BindingAdapter("imageUrl")
fun setImageUrl(imageView: ImageView, url: String?) {
    val requestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    try {
        Glide.with(imageView.context)
            .load(url)
            .placeholder(R.drawable.poster_placeholder)
            .error(R.drawable.poster_placeholder)
            .fallback(R.drawable.poster_placeholder)
            .apply(requestOptions)
            .into(imageView)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

@BindingAdapter("wideImageUrl")
fun setWideImageUrl(imageView: ImageView, showDetailArg: ShowDetailArg) {
    val requestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    try {
        Glide.with(imageView.context)
            .load(showDetailArg.showBackgroundUrl ?: showDetailArg.showImageUrl)
            .placeholder(R.drawable.backdrop_background)
            .error(R.drawable.backdrop_background)
            .fallback(R.drawable.backdrop_background)
            .apply(requestOptions)
            .into(imageView)
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}