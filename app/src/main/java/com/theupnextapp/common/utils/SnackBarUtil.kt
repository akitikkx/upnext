package com.theupnextapp.common.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackBarUtil {

    fun showSnackBar(
        view: View,
        snackBarText: CharSequence,
        actionMessage: String?,
        duration: Int = Snackbar.LENGTH_LONG,
        listener: View.OnClickListener?
    ) {
        val snackBar = Snackbar.make(
            view,
            snackBarText,
            duration
        )
        if (!actionMessage.isNullOrEmpty() && listener != null) {
            snackBar.setAction(actionMessage, listener)
        }
        snackBar.show()
    }

}