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

package com.theupnextapp.common.utils

import android.content.Context
import android.view.View
import com.theupnextapp.R

class Feedback(val context: Context) {

    fun showSnackBar(
        view: View,
        type: FeedBackStatus,
        duration: Int,
        listener: View.OnClickListener?
    ) {
        SnackBarUtil.showSnackBar(
            view,
            type.getDisplayMessage(this@Feedback.context),
            type.getActionMessage(this@Feedback.context),
            duration,
            listener
        )
    }
}

enum class FeedBackStatus {

    NO_CONNECTION {
        override fun getDisplayMessage(context: Context) =
            context.resources.getString(R.string.error_device_not_connected_to_internet)

        override fun getActionMessage(context: Context) =
            context.resources.getString(R.string.error_connect_to_internet)
    },

    SHOW_SEASONS_EMPTY {
        override fun getDisplayMessage(context: Context) =
            context.resources.getString(R.string.error_show_detail_seasons_empty)

        override fun getActionMessage(context: Context): String = ""
    },

    CONNECTION_TO_TRAKT_REQUIRED {
        override fun getDisplayMessage(context: Context) =
            context.resources.getString(R.string.error_trakt_account_connection_required)

        override fun getActionMessage(context: Context): String =
            context.resources.getString(R.string.error_connect_trakt_account)
    };

    abstract fun getDisplayMessage(context: Context): String

    abstract fun getActionMessage(context: Context): String
}