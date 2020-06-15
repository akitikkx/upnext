package com.theupnextapp.common.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar


fun showSnackBar(
    view: View,
    snackBarText: CharSequence,
    duration: Int
) {
    val snackBar = Snackbar.make(
        view,
        snackBarText,
        duration
    )
    snackBar.show()
}

fun showSnackBarWithAction(
    view: View,
    snackBarText: CharSequence,
    actionMessage: String?,
    listener: View.OnClickListener
) {
    val snackBar = Snackbar.make(
        view,
        snackBarText,
        Snackbar.LENGTH_LONG
    )
    snackBar.setAction(actionMessage, listener)
    snackBar.show()
}