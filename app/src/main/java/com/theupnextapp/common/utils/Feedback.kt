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