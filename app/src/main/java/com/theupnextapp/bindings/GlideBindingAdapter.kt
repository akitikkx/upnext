/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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